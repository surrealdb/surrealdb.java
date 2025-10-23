use std::hash::{DefaultHasher, Hash, Hasher};
use std::ptr::null_mut;
use std::str::FromStr;

use jni::objects::{JClass, JString};
use jni::sys::{jboolean, jint, jlong, jstring};
use jni::JNIEnv;
use surrealdb::types::{RecordId, RecordIdKey, ToSql, Uuid, Value};

use crate::error::SurrealError;
use crate::{get_rust_string, get_value_instance, new_string, JniTypes};

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_RecordId_newThingLongId<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    table: JString<'local>,
    id: jlong,
) -> jlong {
    let table = get_rust_string!(&mut env, table, || 0);
    let value = Value::RecordId(RecordId::new(table, RecordIdKey::Number(id)));
    JniTypes::new_value(value.into())
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_RecordId_newThingStringId<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    table: JString<'local>,
    id: JString<'local>,
) -> jlong {
    let table = get_rust_string!(&mut env, table, || 0);
    let id = get_rust_string!(&mut env, id, || 0);
    let value = Value::RecordId(RecordId::new(table, RecordIdKey::String(id)));
    JniTypes::new_value(value.into())
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_RecordId_newThingUuidId<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    table: JString<'local>,
    id: JString<'local>,
) -> jlong {
    let table = get_rust_string!(&mut env, table, || 0);
    let id = get_rust_string!(&mut env, id, || 0);
    if let Ok(uuid) = Uuid::from_str(&id) {
        let value = Value::RecordId(RecordId::new(table, RecordIdKey::Uuid(uuid)));
        JniTypes::new_value(value.into())
    } else {
        SurrealError::NullPointerException("Thing").exception(&mut env, || 0)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_RecordId_getTable<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    let value = get_value_instance!(&mut env, ptr, null_mut);
    if let Value::RecordId(o) = value.as_ref() {
        new_string!(&mut env, o.table.to_sql(), null_mut)
    } else {
        SurrealError::NullPointerException("Thing").exception(&mut env, null_mut)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_RecordId_getId<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    let value = get_value_instance!(&mut env, ptr, || 0);
    if let Value::RecordId(_) = value.as_ref() {
        JniTypes::new_value(value)
    } else {
        SurrealError::NullPointerException("Thing").exception(&mut env, || 0)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_RecordId_equals<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr1: jlong,
    ptr2: jlong,
) -> jboolean {
    let v1 = get_value_instance!(&mut env, ptr1, || false as jboolean);
    let v2 = get_value_instance!(&mut env, ptr2, || false as jboolean);
    if let (Value::RecordId(t1), Value::RecordId(t2)) = (v1.as_ref(), v2.as_ref()) {
        return t1.eq(t2) as jboolean;
    }
    SurrealError::NullPointerException("Thing").exception(&mut env, || false as jboolean)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_RecordId_hashCode<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jint {
    let value = get_value_instance!(&mut env, ptr, || 0);
    if let Value::RecordId(o) = value.as_ref() {
        let mut hasher = DefaultHasher::new();
        o.hash(&mut hasher);
        let hash64 = hasher.finish();
        return (hash64 & 0xFFFFFFFF) as jint;
    }
    SurrealError::NullPointerException("Thing").exception(&mut env, || 0)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_RecordId_toString<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    let value = get_value_instance!(&mut env, ptr, null_mut);
    if let Value::RecordId(o) = value.as_ref() {
        return new_string!(&mut env, o.to_sql(), null_mut);
    }
    SurrealError::NullPointerException("Thing").exception(&mut env, null_mut)
}
