use std::ptr::null_mut;
use std::sync::Arc;

use jni::JNIEnv;
use jni::objects::JClass;
use jni::sys::{jboolean, jint, jlong, jstring};
use surrealdb::sql::Value;

use crate::{create_instance, get_value_instance, new_string, release_instance};
use crate::error::SurrealError;

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Array_deleteInstance<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
    id: jlong,
) -> jboolean {
    release_instance::<Arc<Value>>(id);
    true as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Array_toString<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    id: jlong,
) -> jstring {
    let value = get_value_instance!(&mut env, id, ||null_mut());
    if matches!(value.as_ref(), Value::Array(_)) {
        new_string!(&mut env, value.to_string(), ||null_mut())
    } else {
        SurrealError::NullPointerException("Array").exception(&mut env, || null_mut())
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Array_toPrettyString<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    id: jlong,
) -> jstring {
    let value = get_value_instance!(&mut env, id, ||null_mut());
    if matches!(value.as_ref(), Value::Array(_)) {
        let s = format!("{value:#}");
        new_string!(&mut env, s, ||null_mut())
    } else {
        SurrealError::NullPointerException("Array").exception(&mut env, || null_mut())
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Array_len<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    id: jlong,
) -> jint {
    let value = get_value_instance!(&mut env, id, ||0 as jint);
    if let Value::Array(a) = value.as_ref() {
        a.len() as jint
    } else {
        SurrealError::NullPointerException("Array").exception(&mut env, || 0)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Array_get<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    id: jlong,
    idx: jint,
) -> jlong {
    let value = get_value_instance!(&mut env, id, ||0);
    if let Value::Array(a) = value.as_ref() {
        // TODO Avoid cloning
        let val = a.get(idx as usize).cloned().unwrap_or(Value::None);
        create_instance(Arc::new(val))
    } else {
        SurrealError::NullPointerException("Array").exception(&mut env, || 0)
    }
}



