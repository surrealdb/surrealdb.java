use std::collections::HashMap;
use std::sync::Arc;
use std::sync::atomic::{AtomicI32, Ordering};

use jni::JNIEnv;
use jni::objects::{JClass, JObject, JString};
use jni::sys::{jint, jobject};
use once_cell::sync::Lazy;
use parking_lot::RwLock;
use surrealdb::engine::any::Any;
use surrealdb::Surreal;
use tokio::runtime::Runtime;

use crate::error::DriverError;

mod error;

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
        Err(e) => return DriverError::from(e).exception(&mut env),
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
    // Extract the connection string
    let input: String = match env.get_string(&input) {
        Ok(i) => i.into(),
        Err(e) => return DriverError::from(e).exception(&mut env),
    };
    println!("INPUT {input}");
    // Retrieve the Surreal instance
    let surreal = match get_surreal_instance(&mut env, &object) {
        Ok(s) => s,
        Err(e) => return e.exception(&mut env)
    };
    println!("CONNECTING...");
    // Connect
    if let Err(err) = TOKIO_RUNTIME.block_on(async { surreal.connect(input).await }) {
        return DriverError::from(err).exception(&mut env);
    }
    std::ptr::null_mut()
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_signin<'local>(
    mut env: JNIEnv<'local>,
    object: JObject<'local>,
) -> jobject {
    // Retrieve the Surreal instance
    let _surreal = match get_surreal_instance(&mut env, &object) {
        Ok(s) => s,
        Err(e) => return e.exception(&mut env)
    };
    todo!()
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_close<'local>(
    mut env: JNIEnv<'local>,
    object: JObject<'local>,
) -> jobject {
    println!("CLOSE");
    // Retrieve the Surreal instance ID
    let id = match get_surreal_id(&mut env, &object) {
        Ok(i) => i,
        Err(e) => return e.exception(&mut env)
    };
    // Remove the Surreal instance
    INSTANCES.write().remove(&id);
    std::ptr::null_mut()
}

fn get_surreal_id(env: &mut JNIEnv, object: &JObject) -> Result<i32, DriverError> {
    Ok(env.get_field(object, "id", "I")?.i()?)
}

fn get_surreal_instance(env: &mut JNIEnv, object: &JObject) -> Result<Arc<Surreal<Any>>, DriverError> {
    let id = get_surreal_id(env, object)?;
    if let Some(i) = INSTANCES.read().get(&id).cloned() {
        Ok(i)
    } else {
        Err(DriverError::InstanceNotFound(id))
    }
}
