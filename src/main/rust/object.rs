use std::hash::{DefaultHasher, Hash, Hasher};
use std::ptr::null_mut;
use std::sync::Arc;

use crate::with_env_body;
use jni::objects::{JClass, JString};
use jni::sys::{jboolean, jint, jlong, jstring};
use jni::EnvUnowned;
use parking_lot::Mutex;
use surrealdb::types::{ToSql, Value};

use crate::error::SurrealError;
use crate::{get_rust_string, get_value_instance, new_string, release_instance, JniTypes};

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Object_deleteInstance<'local>(
    _env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) {
    release_instance::<Arc<Value>>(ptr);
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Object_len<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jint {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || 0 as jint);
        if let Value::Object(o) = value.as_ref() {
            o.len() as jint
        } else {
            SurrealError::NullPointerException("Object").exception(env, || 0)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Object_get<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    key: JString<'local>,
) -> jlong {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || 0);
        if let Value::Object(o) = value.as_ref() {
            let key = get_rust_string!(env, key, || 0);
            // TODO Avoid cloning
            let val = o.get(&key).cloned().unwrap_or(Value::None);
            JniTypes::new_value(val.into())
        } else {
            SurrealError::NullPointerException("Object").exception(env, || 0)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Object_iterator<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || 0);
        if let Value::Object(o) = value.as_ref() {
            let iter = o.clone().into_iter();
            JniTypes::new_object_iter(iter)
        } else {
            SurrealError::NullPointerException("Object").exception(env, || 0)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Object_synchronizedIterator<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || 0);
        if let Value::Object(o) = value.as_ref() {
            let iter = o.clone().into_iter();
            JniTypes::new_sync_object_iter(Arc::new(Mutex::new(iter)))
        } else {
            SurrealError::NullPointerException("Object").exception(env, || 0)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Object_toString<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, null_mut);
        if matches!(value.as_ref(), Value::Object(_)) {
            new_string!(env, value.to_sql(), null_mut)
        } else {
            SurrealError::NullPointerException("Object").exception(env, null_mut)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Object_hashCode<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jint {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || 0);
        if let Value::Object(o) = value.as_ref() {
            let mut hasher = DefaultHasher::new();
            o.hash(&mut hasher);
            let hash64 = hasher.finish();
            return (hash64 & 0xFFFFFFFF) as jint;
        }
        SurrealError::NullPointerException("Object").exception(env, || 0)
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Object_equals<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr1: jlong,
    ptr2: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let v1 = get_value_instance!(env, ptr1, || false as jboolean);
        let v2 = get_value_instance!(env, ptr2, || false as jboolean);
        if let (Value::Object(o1), Value::Object(o2)) = (v1.as_ref(), v2.as_ref()) {
            return o1.eq(o2) as jboolean;
        }
        SurrealError::NullPointerException("Object").exception(env, || false as jboolean)
    })
}
