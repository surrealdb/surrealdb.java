use std::collections::HashMap;
use std::sync::Arc;
use std::sync::atomic::{AtomicI32, Ordering};

use jni::JNIEnv;
use jni::objects::{JClass, JString};
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
    check_exception(&mut env, None);
    println!("INSTANCE CREATED");
    instance
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_connect<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    id: jint,
    input: JString<'local>,
) {
    println!("CONNECT");
    // Extract the connection string
    let input: String = match env.get_string(&input) {
        Ok(i) => i.into(),
        Err(_) => {
            check_exception(
                &mut env,
                Some(("java/lang/IllegalArgumentException", "Invalid string input")),
            );
            return;
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
            return;
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
}

fn check_exception(env: &mut JNIEnv, t: Option<(&str, &str)>) {
    if let Ok(b) = env.exception_check() {
        if b {
            let _ = env.exception_describe();
            let _ = env.exception_clear();
        }
        if let Some(t) = t {
            let _ = env.throw(t);
        }
    }
}
