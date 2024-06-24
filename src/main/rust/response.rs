use std::sync::Arc;

use jni::JNIEnv;
use jni::objects::JClass;
use jni::sys::{jboolean, jint, jlong};
use parking_lot::Mutex;
use surrealdb::Response;
use surrealdb::sql::Value;

use crate::{create_instance, get_response_instance, release_instance};
use crate::error::SurrealError;

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Response_deleteInstance<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    release_instance::<Arc<Mutex<Response>>>(ptr);
    true as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Response_take<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    num: jint,
) -> jlong {
    let response = get_response_instance!(&mut env, ptr, || 0);
    let value: Value = match response.lock().take(num as usize) {
        Ok(r) => r,
        Err(e) => return SurrealError::SurrealDB(e).exception(&mut env, || 0),
    };
    create_instance(Arc::new(value))
}