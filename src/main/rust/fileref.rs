use std::ptr::null_mut;

use jni::objects::JClass;
use jni::sys::{jlong, jstring};
use jni::JNIEnv;
use surrealdb::types::Value;

use crate::error::SurrealError;
use crate::{get_value_instance, new_string, release_instance};

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_FileRef_getBucket<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    let value = get_value_instance!(&mut env, ptr, null_mut);
    if let Value::File(f) = value.as_ref() {
        new_string!(&mut env, f.bucket().to_string(), null_mut)
    } else {
        SurrealError::NullPointerException("FileRef").exception(&mut env, null_mut)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_FileRef_getKey<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    let value = get_value_instance!(&mut env, ptr, null_mut);
    if let Value::File(f) = value.as_ref() {
        new_string!(&mut env, f.key().to_string(), null_mut)
    } else {
        SurrealError::NullPointerException("FileRef").exception(&mut env, null_mut)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_FileRef_toString<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    let value = get_value_instance!(&mut env, ptr, null_mut);
    if let Value::File(f) = value.as_ref() {
        let s = format!("f\"{}:{}\"", f.bucket(), f.key());
        new_string!(&mut env, s, null_mut)
    } else {
        SurrealError::NullPointerException("FileRef").exception(&mut env, null_mut)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_FileRef_deleteInstance<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) {
    release_instance::<std::sync::Arc<Value>>(ptr);
}
