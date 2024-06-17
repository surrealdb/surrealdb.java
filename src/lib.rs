use std::collections::HashMap;
use std::error::Error;
use std::fmt::Debug;
use std::sync::atomic::{AtomicI32, Ordering};
use std::sync::Arc;

use jni::objects::{JClass, JObject, JString};
use jni::signature::{Primitive, ReturnType};
use jni::sys::jvalue;
use jni::JNIEnv;
use once_cell::sync::Lazy;
use parking_lot::RwLock;
use surrealdb::engine::any::Any;
use surrealdb::Surreal;
use tokio::runtime::Runtime;

static TOKIO_RUNTIME: Lazy<Runtime> =
    Lazy::new(|| Runtime::new().expect("Cannot start Tokio runtime"));

static INSTANCES: Lazy<RwLock<HashMap<i32, Arc<Surreal<Any>>>>> =
    Lazy::new(|| RwLock::new(HashMap::new()));

static ID_SEQUENCE: Lazy<AtomicI32> = Lazy::new(|| AtomicI32::new(0));

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_new_1instance<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
) -> JObject<'local> {
    // Load the Surreal class
    let class = log_err(env.find_class("com/surrealdb/Surreal"));
    // Find the constructor
    let constructor = log_err(env.get_method_id(&class, "<init>", "(I)V"));
    // Attribute a new ID to each new instance
    let id = ID_SEQUENCE.fetch_add(1, Ordering::Relaxed);
    // Store the instance
    INSTANCES.write().insert(id, Arc::new(Surreal::init()));
    // Build the new instance
    let instance: JObject =
        log_err(unsafe { env.new_object_unchecked(&class, constructor, &[jvalue { i: id }]) });
    // Return the instance
    instance
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_connect<'local>(
    mut env: JNIEnv<'local>,
    class: JClass<'local>,
    input: JString<'local>,
) {
    // Extract the connection string
    let input: String = log_err(env.get_string(&input)).into();
    // Get the Surreal instance ID
    let id = log_err(env.get_field_id(&class, "id", "I"));
    let id = log_err(env.get_field_unchecked(&class, id, ReturnType::Primitive(Primitive::Int)));
    let id = log_err(id.i()) as i32;
    // Retrieve the Surreal instance
    let surreal = INSTANCES
        .read()
        .get(&id)
        .cloned()
        .expect("Surreal instance not found");
    // Connect
    log_err(TOKIO_RUNTIME.block_on(async { surreal.connect(input).await }));
}

fn log_err<R, E>(r: Result<R, E>) -> R
where
    E: Debug + Error,
{
    r.unwrap_or_else(|e| panic!("{e}"))
}
