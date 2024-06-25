use std::hash::{DefaultHasher, Hash, Hasher};
use std::ptr::null_mut;
use std::sync::Arc;

use jni::objects::JClass;
use jni::sys::{jboolean, jint, jlong, jstring};
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
            return new_string!(&mut env, s, null_mut);
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

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Id_toString<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    let value = get_value_instance!(&mut env, ptr, null_mut);
    if let Value::Thing(o) = value.as_ref() {
        let s = o.id.to_string();
        return new_string!(&mut env, s, null_mut);
    }
    SurrealError::NullPointerException("Id").exception(&mut env, null_mut)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Id_hashCode<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jint {
    let value = get_value_instance!(&mut env, ptr, || 0);
    if let Value::Thing(o) = value.as_ref() {
        let mut hasher = DefaultHasher::new();
        o.id.hash(&mut hasher);
        let hash64 = hasher.finish();
        return (hash64 & 0xFFFFFFFF) as jint;
    }
    SurrealError::NullPointerException("Id").exception(&mut env, || 0)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Id_equals<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr1: jlong,
    ptr2: jlong,
) -> jboolean {
    let v1 = get_value_instance!(&mut env, ptr1, || false as jboolean);
    let v2 = get_value_instance!(&mut env, ptr2, || false as jboolean);
    if let (Value::Thing(t1), Value::Thing(t2)) = (v1.as_ref(), v2.as_ref()) {
        return t1.id.eq(&t2.id) as jboolean;
    }
    SurrealError::NullPointerException("Id").exception(&mut env, || false as jboolean)
}
