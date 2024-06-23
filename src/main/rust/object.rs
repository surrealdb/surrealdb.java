use std::ptr::null_mut;
use std::sync::Arc;

use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::{jboolean, jint, jlong, jstring};
use parking_lot::Mutex;
use surrealdb::sql::Value;

use crate::{create_instance, get_rust_string, get_value_instance, new_string, release_instance};
use crate::error::SurrealError;

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Object_deleteInstance<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
    id: jlong,
) -> jboolean {
    release_instance::<Arc<Value>>(id);
    true as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Object_toString<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    id: jlong,
) -> jstring {
    let value = get_value_instance!(&mut env, id, ||null_mut());
    if matches!(value.as_ref(), Value::Object(_)) {
        new_string!(&mut env, value.to_string(), ||null_mut())
    } else {
        SurrealError::NullPointerException("Object").exception(&mut env, || null_mut())
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Object_toPrettyString<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    id: jlong,
) -> jstring {
    let value = get_value_instance!(&mut env, id, ||null_mut());
    if matches!(value.as_ref(), Value::Object(_)) {
        let s = format!("{value:#}");
        new_string!(&mut env, s, ||null_mut())
    } else {
        SurrealError::NullPointerException("Object").exception(&mut env, || null_mut())
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Object_len<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    id: jlong,
) -> jint {
    let value = get_value_instance!(&mut env, id, ||0 as jint);
    if let Value::Object(o) = value.as_ref() {
        o.len() as jint
    } else {
        SurrealError::NullPointerException("Object").exception(&mut env, || 0)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Object_get<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    id: jlong,
    key: JString<'local>,
) -> jlong {
    let value = get_value_instance!(&mut env, id, ||0);
    if let Value::Object(o) = value.as_ref() {
        let key = get_rust_string!(&mut env, key, ||0);
        // TODO Avoid cloning
        let val = o.get(&key).cloned().unwrap_or(Value::None);
        create_instance(Arc::new(val))
    } else {
        SurrealError::NullPointerException("Object").exception(&mut env, || 0)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Object_iterator<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    id: jlong,
) -> jlong {
    let value = get_value_instance!(&mut env, id, ||0);
    if let Value::Object(o) = value.as_ref() {
        let iter = o.0.clone().into_iter();
        create_instance(iter)
    } else {
        SurrealError::NullPointerException("Object").exception(&mut env, || 0)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Object_synchronizedIterator<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    id: jlong,
) -> jlong {
    let value = get_value_instance!(&mut env, id, ||0);
    if let Value::Object(o) = value.as_ref() {
        let iter = o.0.clone().into_iter();
        create_instance(Arc::new(Mutex::new(iter)))
    } else {
        SurrealError::NullPointerException("Object").exception(&mut env, || 0)
    }
}


