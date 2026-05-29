use std::hash::{DefaultHasher, Hash, Hasher};
use std::ptr::null_mut;
use std::sync::Arc;

use crate::with_env_body;
use jni::objects::{JClass, JLongArray};
use jni::sys::{jboolean, jint, jlong, jstring};
use jni::EnvUnowned;
use parking_lot::Mutex;
use surrealdb::types::{Array, ToSql, Value};

use crate::error::SurrealError;
use crate::{
    get_long_array, get_value_instance, new_string, release_instance, take_value_mut_instance,
    JniTypes,
};

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Array_deleteInstance<'local>(
    _env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) {
    release_instance::<Arc<Value>>(ptr);
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Array_newOf<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptrs: JLongArray<'local>,
) -> jlong {
    with_env_body!(env, env, {
        let ptrs = get_long_array!(env, &ptrs, || 0);
        let mut values = Vec::with_capacity(ptrs.len());
        for ptr in ptrs {
            let value = take_value_mut_instance!(env, ptr, || 0);
            values.push(value);
        }
        let value = Value::Array(Array::from(values));
        JniTypes::new_value(value.into())
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Array_len<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jint {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || 0 as jint);
        if let Value::Array(a) = value.as_ref() {
            a.len() as jint
        } else {
            SurrealError::NullPointerException("Array").exception(env, || 0)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Array_get<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    idx: jint,
) -> jlong {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || 0);
        if let Value::Array(a) = value.as_ref() {
            let val = a.get(idx as usize).cloned().unwrap_or(Value::None);
            JniTypes::new_value(val.into())
        } else {
            SurrealError::NullPointerException("Array").exception(env, || 0)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Array_iterator<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || 0);
        if let Value::Array(a) = value.as_ref() {
            let iter = a.clone().into_iter();
            JniTypes::new_array_iter(iter)
        } else {
            SurrealError::NullPointerException("Array").exception(env, || 0)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Array_synchronizedIterator<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || 0);
        if let Value::Array(a) = value.as_ref() {
            let iter = a.clone().into_iter();
            JniTypes::new_sync_array_iter(Mutex::new(iter).into())
        } else {
            SurrealError::NullPointerException("Array").exception(env, || 0)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Array_toString<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, null_mut);
        if matches!(value.as_ref(), Value::Array(_)) {
            new_string!(env, value.to_sql(), null_mut)
        } else {
            SurrealError::NullPointerException("Array").exception(env, null_mut)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Array_hashCode<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jint {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || 0);
        if let Value::Array(a) = value.as_ref() {
            let mut hasher = DefaultHasher::new();
            a.hash(&mut hasher);
            let hash64 = hasher.finish();
            return (hash64 & 0xFFFFFFFF) as jint;
        }
        SurrealError::NullPointerException("Array").exception(env, || 0)
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Array_equals<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr1: jlong,
    ptr2: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let v1 = get_value_instance!(env, ptr1, || false as jboolean);
        let v2 = get_value_instance!(env, ptr2, || false as jboolean);
        if let (Value::Array(a1), Value::Array(a2)) = (v1.as_ref(), v2.as_ref()) {
            return a1.eq(a2) as jboolean;
        }
        SurrealError::NullPointerException("Array").exception(env, || false as jboolean)
    })
}
