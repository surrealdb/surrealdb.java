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
        Err(e) => return exception(&mut env, Some(e), None),
    };
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
        Err(e) => return exception(&mut env, Some(e), None),
    };
    // Extract the connection string
    let input: String = match env.get_string(&input) {
        Ok(i) => i.into(),
        Err(e) => return exception(&mut env, Some(e), None),
    };
    println!("INPUT {input}");
    // Retrieve the Surreal instance
    let surreal = match INSTANCES.read().get(&id).cloned() {
        Some(s) => s,
        None => {
            return exception(
                &mut env,
                None,
                Some(("java/lang/IllegalArgumentException", "Invalid Surreal ID")),
            )
        }
    };
    println!("CONNECTING...");
    // Connect
    if let Err(err) = TOKIO_RUNTIME.block_on(async { surreal.connect(input).await }) {
        return exception(
            &mut env,
            None,
            Some(("java/lang/RuntimeException", &format!("{err}"))),
        );
    }
    std::ptr::null_mut()
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
        Err(e) => return exception(&mut env, Some(e), None),
    };
    // Remove the Surreal instance
    INSTANCES.write().remove(&id);
    std::ptr::null_mut()
}

fn get_surrealdb_id(env: &mut JNIEnv, object: &JObject) -> Result<i32, Error> {
    env.get_field(object, "id", "I")?.i()
}

fn exception(env: &mut JNIEnv, e: Option<Error>, t: Option<(&str, &str)>) -> jobject {
    if let Ok(b) = env.exception_check() {
        if !b {
            // There is not already an exception
            if let Some(e) = e {
                let _ = env.throw(format!("{e}"));
            }
            if let Some(t) = t {
                let _ = env.throw(t);
            }
        }
    }
    std::ptr::null_mut()
}
