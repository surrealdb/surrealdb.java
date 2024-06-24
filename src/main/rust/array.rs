use std::ptr::null_mut;
use std::sync::Arc;

use jni::objects::JClass;
use jni::sys::{jboolean, jint, jlong, jstring};
use jni::JNIEnv;
use parking_lot::Mutex;
use surrealdb::sql::Value;

use crate::error::SurrealError;
use crate::{create_instance, get_value_instance, new_string, release_instance};

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Array_deleteInstance<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    release_instance::<Arc<Value>>(ptr);
    true as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Array_toString<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    let value = get_value_instance!(&mut env, ptr, null_mut);
    if matches!(value.as_ref(), Value::Array(_)) {
        new_string!(&mut env, value.to_string(), null_mut)
    } else {
        SurrealError::NullPointerException("Array").exception(&mut env, null_mut)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Array_toPrettyString<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    let value = get_value_instance!(&mut env, ptr, null_mut);
    if matches!(value.as_ref(), Value::Array(_)) {
        let s = format!("{value:#}");
        new_string!(&mut env, s, null_mut)
    } else {
        SurrealError::NullPointerException("Array").exception(&mut env, null_mut)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Array_len<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jint {
    let value = get_value_instance!(&mut env, ptr, || 0 as jint);
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
    ptr: jlong,
    idx: jint,
) -> jlong {
    let value = get_value_instance!(&mut env, ptr, || 0);
    if let Value::Array(a) = value.as_ref() {
        let val = a.get(idx as usize).cloned().unwrap_or(Value::None);
        create_instance(Arc::new(val))
    } else {
        SurrealError::NullPointerException("Array").exception(&mut env, || 0)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Array_iterator<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    let value = get_value_instance!(&mut env, ptr, || 0);
    if let Value::Array(a) = value.as_ref() {
        let iter = a.0.clone().into_iter();
        create_instance(iter)
    } else {
        SurrealError::NullPointerException("Array").exception(&mut env, || 0)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Array_synchronizedIterator<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    let value = get_value_instance!(&mut env, ptr, || 0);
    if let Value::Array(a) = value.as_ref() {
        let iter = a.0.clone().into_iter();
        create_instance(Arc::new(Mutex::new(iter)))
    } else {
        SurrealError::NullPointerException("Array").exception(&mut env, || 0)
    }
}
