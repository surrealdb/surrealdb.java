use std::hash::{DefaultHasher, Hash, Hasher};
use std::ptr::null_mut;
use std::str::FromStr;

use crate::with_env_body;
use jni::objects::{JClass, JString};
use jni::sys::{jboolean, jint, jlong, jstring};
use jni::EnvUnowned;
use surrealdb::types::{RecordId, RecordIdKey, ToSql, Uuid, Value};

use crate::error::SurrealError;
use crate::{get_rust_string, get_value_instance, new_string, release_instance, JniTypes};

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_RecordId_deleteInstance<'local>(
    _env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) {
    release_instance::<std::sync::Arc<Value>>(ptr);
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_RecordId_newRecordIdWithLong<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    table: JString<'local>,
    id: jlong,
) -> jlong {
    with_env_body!(env, env, {
        let table = get_rust_string!(env, table, || 0);
        let value = Value::RecordId(RecordId::new(table, RecordIdKey::Number(id)));
        JniTypes::new_value(value.into())
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_RecordId_newRecordIdWithString<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    table: JString<'local>,
    id: JString<'local>,
) -> jlong {
    with_env_body!(env, env, {
        let table = get_rust_string!(env, table, || 0);
        let id = get_rust_string!(env, id, || 0);
        let value = Value::RecordId(RecordId::new(table, RecordIdKey::String(id)));
        JniTypes::new_value(value.into())
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_RecordId_newRecordIdWithUuid<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    table: JString<'local>,
    id: JString<'local>,
) -> jlong {
    with_env_body!(env, env, {
        let table = get_rust_string!(env, table, || 0);
        let id = get_rust_string!(env, id, || 0);
        if let Ok(uuid) = Uuid::from_str(&id) {
            let value = Value::RecordId(RecordId::new(table, RecordIdKey::Uuid(uuid)));
            JniTypes::new_value(value.into())
        } else {
            SurrealError::NullPointerException("RecordId").exception(env, || 0)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_RecordId_newRecordIdWithArray<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    table: JString<'local>,
    array_ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        let table = get_rust_string!(env, table, || 0);
        let value = get_value_instance!(env, array_ptr, || 0);
        if let Value::Array(a) = value.as_ref() {
            let key = RecordIdKey::Array(a.clone());
            let value = Value::RecordId(RecordId::new(table, key));
            JniTypes::new_value(value.into())
        } else {
            SurrealError::NullPointerException("RecordId").exception(env, || 0)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_RecordId_newRecordIdWithObject<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    table: JString<'local>,
    object_ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        let table = get_rust_string!(env, table, || 0);
        let value = get_value_instance!(env, object_ptr, || 0);
        if let Value::Object(o) = value.as_ref() {
            let key = RecordIdKey::Object(o.clone());
            let value = Value::RecordId(RecordId::new(table, key));
            JniTypes::new_value(value.into())
        } else {
            SurrealError::NullPointerException("RecordId").exception(env, || 0)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_RecordId_getTable<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, null_mut);
        if let Value::RecordId(o) = value.as_ref() {
            new_string!(env, o.table.to_sql(), null_mut)
        } else {
            SurrealError::NullPointerException("RecordId").exception(env, null_mut)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_RecordId_getId<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || 0);
        if let Value::RecordId(_) = value.as_ref() {
            JniTypes::new_value(value)
        } else {
            SurrealError::NullPointerException("RecordId").exception(env, || 0)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_RecordId_equals<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr1: jlong,
    ptr2: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let v1 = get_value_instance!(env, ptr1, || false as jboolean);
        let v2 = get_value_instance!(env, ptr2, || false as jboolean);
        if let (Value::RecordId(t1), Value::RecordId(t2)) = (v1.as_ref(), v2.as_ref()) {
            return t1.eq(t2) as jboolean;
        }
        SurrealError::NullPointerException("RecordId").exception(env, || false as jboolean)
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_RecordId_hashCode<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jint {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || 0);
        if let Value::RecordId(o) = value.as_ref() {
            let mut hasher = DefaultHasher::new();
            o.hash(&mut hasher);
            let hash64 = hasher.finish();
            return (hash64 & 0xFFFFFFFF) as jint;
        }
        SurrealError::NullPointerException("RecordId").exception(env, || 0)
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_RecordId_toString<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, null_mut);
        if let Value::RecordId(o) = value.as_ref() {
            return new_string!(env, o.to_sql(), null_mut);
        }
        SurrealError::NullPointerException("RecordId").exception(env, null_mut)
    })
}
