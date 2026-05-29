use std::ptr::null_mut;

use crate::with_env_body;
use jni::objects::JClass;
use jni::sys::{jlong, jstring};
use jni::EnvUnowned;
use surrealdb::types::Value;

use crate::error::SurrealError;
use crate::{get_value_instance, new_string, release_instance};

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_FileRef_getBucket<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, null_mut);
        if let Value::File(f) = value.as_ref() {
            new_string!(env, f.bucket(), null_mut)
        } else {
            SurrealError::NullPointerException("FileRef").exception(env, null_mut)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_FileRef_getKey<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, null_mut);
        if let Value::File(f) = value.as_ref() {
            new_string!(env, f.key(), null_mut)
        } else {
            SurrealError::NullPointerException("FileRef").exception(env, null_mut)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_FileRef_toString<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, null_mut);
        if let Value::File(f) = value.as_ref() {
            let s = format!("f\"{}:{}\"", f.bucket(), f.key());
            new_string!(env, s, null_mut)
        } else {
            SurrealError::NullPointerException("FileRef").exception(env, null_mut)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_FileRef_deleteInstance<'local>(
    _env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) {
    release_instance::<std::sync::Arc<Value>>(ptr);
}
