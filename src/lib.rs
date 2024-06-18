use std::collections::HashMap;
use std::sync::Arc;
use std::sync::atomic::{AtomicI32, Ordering};

use jni::errors::Error;
use jni::JNIEnv;
use jni::objects::{JClass, JObject, JString};
use jni::sys::{jint, jobject};
use once_cell::sync::Lazy;
use parking_lot::RwLock;
use surrealdb::engine::any::Any;
use surrealdb::Surreal;
use tokio::runtime::Runtime;

static TOKIO_RUNTIME: Lazy<Runtime> =
    Lazy::new(|| Runtime::new().expect("Cannot start Tokio runtime"));

static INSTANCES: Lazy<RwLock<HashMap<i32, Arc<Surreal<Any>>>>> =
    Lazy::new(|| RwLock::new(HashMap::new()));

static ID_SEQUENCE: Lazy<AtomicI32> = Lazy::new(|| AtomicI32::new(1));

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_new_1instance<'local>(
    mut env: JNIEnv<'local>,
    class: JClass<'local>,
) -> jobject {
    // Attribute a new ID to each new instance
    let id = ID_SEQUENCE.fetch_add(1, Ordering::Relaxed);
    // Store the instance
    INSTANCES.write().insert(id, Arc::new(Surreal::init()));
    let id = id as jint;
    // Build the new instance
    let instance = match env.new_object(class, "(I)V", &[id.into()]) {
        Ok(i) => i.into_raw(),
        Err(e) => {
            eprintln!("{e}");
            return std::ptr::null_mut();
        }
    };
    if check_exception(&mut env, None) {
        return std::ptr::null_mut();
    }
    println!("INSTANCE CREATED");
    instance
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_connect<'local>(
    mut env: JNIEnv<'local>,
    object: JObject<'local>,
    input: JString<'local>,
) -> jobject {
    println!("CONNECT");
    // Retrieve the Surreal instance ID
    let id = match get_surrealdb_id(&mut env, &object) {
        Ok(i) => i,
        Err(e) => {
            check_exception(
                &mut env,
                Some(("java/lang/RuntimeException", &format!("{e}"))),
            );
            return std::ptr::null_mut();
        }
    };

    // Extract the connection string
    let input: String = match env.get_string(&input) {
        Ok(i) => i.into(),
        Err(_) => {
            check_exception(
                &mut env,
                Some(("java/lang/IllegalArgumentException", "Invalid string input")),
            );
            return std::ptr::null_mut();
        }
    };
    println!("INPUT {input}");
    // Retrieve the Surreal instance
    let surreal = match INSTANCES.read().get(&id).cloned() {
        None => {
            check_exception(
                &mut env,
                Some(("java/lang/IllegalArgumentException", "Invalid Surreal ID")),
            );
            return std::ptr::null_mut();
        }
        Some(s) => s,
    };
    println!("CONNECTING...");
    // Connect
    if let Err(err) = TOKIO_RUNTIME.block_on(async { surreal.connect(input).await }) {
        check_exception(
            &mut env,
            Some(("java/lang/RuntimeException", &format!("{err}"))),
        );
    }
    return std::ptr::null_mut();
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_close<'local>(
    mut env: JNIEnv<'local>,
    object: JObject<'local>,
) -> jobject {
    println!("CLOSE");
    // Retrieve the Surreal instance ID
    let id = match get_surrealdb_id(&mut env, &object) {
        Ok(i) => i,
        Err(e) => {
            check_exception(
                &mut env,
                Some(("java/lang/RuntimeException", &format!("{e}"))),
            );
            return std::ptr::null_mut();
        }
    };

    // Remove the Surreal instance
    INSTANCES.write().remove(&id);
    return std::ptr::null_mut();
}

fn get_surrealdb_id<'local>(env: &mut JNIEnv, object: &JObject<'local>) -> Result<i32, Error> {
    let id = env.get_field(object, "id", "I")?;
    Ok(id.i()? as i32)
}

fn return_exception(env: &mut JNIEnv, t: Option<(&str, &str)>) -> jobject {
    check_exception(env, t);
    return std::ptr::null_mut();
}

fn check_exception(env: &mut JNIEnv, t: Option<(&str, &str)>) -> bool {
    if let Ok(b) = env.exception_check() {
        if b { // There is already an exception
            return true;
        }
        if let Some(t) = t {
            let _ = env.throw(t);
            return true;
        }
    }
    false
}
