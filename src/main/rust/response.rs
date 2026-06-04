use std::sync::Arc;

use crate::with_env_body;
use jni::objects::JClass;
use jni::sys::{jint, jlong};
use jni::EnvUnowned;
use parking_lot::Mutex;
use surrealdb::types::Value;
use surrealdb::{IndexedResults, Result};

use crate::error::SurrealError;
use crate::{create_instance, get_response_instance, release_instance, JniTypes};

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Response_deleteInstance<'local>(
    _env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) {
    release_instance::<Arc<Mutex<Result<IndexedResults>>>>(ptr);
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Response_take<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    num: jint,
) -> jlong {
    with_env_body!(env, env, {
        let response = get_response_instance!(env, ptr, || 0);
        let value: Value = match response.lock().take(num as usize) {
            Ok(r) => r,
            Err(e) => return SurrealError::SurrealDB(e).exception(env, || 0),
        };
        create_instance(Arc::new(value), JniTypes::Value)
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Response_size<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jint {
    with_env_body!(env, env, {
        let response = get_response_instance!(env, ptr, || 0);
        return response.lock().num_statements() as jint;
    })
}
