use std::hash::{DefaultHasher, Hash, Hasher};
use std::ptr::null_mut;
use std::sync::Arc;

use jni::objects::JClass;
use jni::sys::{jboolean, jint, jlong, jstring};
use jni::JNIEnv;
use parking_lot::Mutex;
use surrealdb::types::{ToSql, Value};

use crate::error::SurrealError;
use crate::{get_value_instance, new_string, release_instance, JniTypes};

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Array_deleteInstance<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) {
    release_instance::<Arc<Value>>(ptr);
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Array_toPrettyString<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    let value = get_value_instance!(&mut env, ptr, null_mut);
    if matches!(value.as_ref(), Value::Array(_)) {
        // TODO pretty print
        let s = value.to_sql();
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
        JniTypes::new_value(val.into())
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
        let iter = a.clone().into_iter();
        JniTypes::new_array_iter(iter)
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
        let iter = a.clone().into_iter();
        JniTypes::new_sync_array_iter(Mutex::new(iter).into())
    } else {
        SurrealError::NullPointerException("Array").exception(&mut env, || 0)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Array_toString<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    let value = get_value_instance!(&mut env, ptr, null_mut);
    if matches!(value.as_ref(), Value::Array(_)) {
        new_string!(&mut env, value.to_sql(), null_mut)
    } else {
        SurrealError::NullPointerException("Array").exception(&mut env, null_mut)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Array_hashCode<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jint {
    let value = get_value_instance!(&mut env, ptr, || 0);
    if let Value::Array(a) = value.as_ref() {
        let mut hasher = DefaultHasher::new();
        a.hash(&mut hasher);
        let hash64 = hasher.finish();
        return (hash64 & 0xFFFFFFFF) as jint;
    }
    SurrealError::NullPointerException("Array").exception(&mut env, || 0)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Array_equals<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr1: jlong,
    ptr2: jlong,
) -> jboolean {
    let v1 = get_value_instance!(&mut env, ptr1, || false as jboolean);
    let v2 = get_value_instance!(&mut env, ptr2, || false as jboolean);
    if let (Value::Array(a1), Value::Array(a2)) = (v1.as_ref(), v2.as_ref()) {
        return a1.eq(a2) as jboolean;
    }
    SurrealError::NullPointerException("Array").exception(&mut env, || false as jboolean)
}
