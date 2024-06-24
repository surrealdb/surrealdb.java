use std::ptr::null_mut;
use std::sync::Arc;

use jni::objects::JClass;
use jni::sys::{jboolean, jlong, jstring};
use jni::JNIEnv;
use surrealdb::sql::{Id, Value};

use crate::error::SurrealError;
use crate::{create_instance, get_value_instance, new_string};

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Id_isLong<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    let value = get_value_instance!(&mut env, ptr, || false as jboolean);
    if let Value::Thing(o) = value.as_ref() {
        if let Id::Number(_) = &o.id {
            true as jboolean
        } else {
            false as jboolean
        }
    } else {
        SurrealError::NullPointerException("Thing").exception(&mut env, || false as jboolean)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Id_getLong<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    let value = get_value_instance!(&mut env, ptr, || 0);
    if let Value::Thing(o) = value.as_ref() {
        if let Id::Number(i) = &o.id {
            return *i as jlong;
        }
    }
    SurrealError::NullPointerException("Thing").exception(&mut env, || 0)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Id_isString<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    let value = get_value_instance!(&mut env, ptr, || false as jboolean);
    if let Value::Thing(o) = value.as_ref() {
        if let Id::String(_) = &o.id {
            true as jboolean
        } else {
            false as jboolean
        }
    } else {
        SurrealError::NullPointerException("Thing").exception(&mut env, || false as jboolean)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Id_getString<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    let value = get_value_instance!(&mut env, ptr, null_mut);
    if let Value::Thing(o) = value.as_ref() {
        if let Id::String(s) = &o.id {
            new_string!(&mut env, s, null_mut)
        }
    }
    SurrealError::NullPointerException("Thing").exception(&mut env, null_mut)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Id_isObject<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    let value = get_value_instance!(&mut env, ptr, || false as jboolean);
    if let Value::Thing(o) = value.as_ref() {
        if let Id::Object(_) = &o.id {
            true as jboolean
        } else {
            false as jboolean
        }
    } else {
        SurrealError::NullPointerException("Thing").exception(&mut env, || false as jboolean)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Id_getObject<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    let value = get_value_instance!(&mut env, ptr, || 0);
    if let Value::Thing(o) = value.as_ref() {
        if let Id::Object(o) = &o.id {
            //TODO no clone?
            return create_instance(Arc::new(Value::Object(o.clone())));
        }
    }
    SurrealError::NullPointerException("Thing").exception(&mut env, || 0)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Id_isArray<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    let value = get_value_instance!(&mut env, ptr, || false as jboolean);
    if let Value::Thing(o) = value.as_ref() {
        if let Id::Array(_) = &o.id {
            true as jboolean
        } else {
            false as jboolean
        }
    } else {
        SurrealError::NullPointerException("Thing").exception(&mut env, || false as jboolean)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Id_getArray<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    let value = get_value_instance!(&mut env, ptr, || 0);
    if let Value::Thing(o) = value.as_ref() {
        if let Id::Array(a) = &o.id {
            //TODO no clone?
            return create_instance(Arc::new(Value::Array(a.clone())));
        }
    }
    SurrealError::NullPointerException("Thing").exception(&mut env, || 0)
}
