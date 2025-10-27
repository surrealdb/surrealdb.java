use std::collections::BTreeMap;
use std::hash::{DefaultHasher, Hash, Hasher};
use std::ptr::null_mut;
use std::str::FromStr;

use chrono::DateTime;
use jni::objects::{JClass, JLongArray, JString};
use jni::sys::{jboolean, jdouble, jint, jlong, jstring};
use jni::JNIEnv;
use rust_decimal::Decimal;
use surrealdb::types::{Array, Datetime, Duration, Number, Object, ToSql, Uuid, Value};

use crate::error::SurrealError;
use crate::{
    create_instance, get_long_array, get_rust_string, get_value_instance, get_value_mut_instance,
    new_string, take_entry_mut_instance, take_value_mut_instance, JniTypes,
};

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_ValueMut_newNone<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
) -> jlong {
    create_instance(Value::None, JniTypes::ValueMut)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_ValueMut_newNull<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
) -> jlong {
    create_instance(Value::Null, JniTypes::ValueMut)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_ValueMut_newString<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    s: JString<'local>,
) -> jlong {
    let s = get_rust_string!(&mut env, s, || 0);
    let value = Value::String(s);
    create_instance(value, JniTypes::ValueMut)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_ValueMut_newLong<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
    l: jlong,
) -> jlong {
    let value = Value::Number(Number::Int(l));
    JniTypes::new_value_mut(value)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_ValueMut_newDouble<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
    d: jdouble,
) -> jlong {
    let value = Value::Number(Number::Float(d));
    JniTypes::new_value_mut(value)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_ValueMut_newBoolean<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
    b: jboolean,
) -> jlong {
    let value = Value::Bool(b == 1);
    JniTypes::new_value_mut(value)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_ValueMut_newDecimal<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    s: JString<'local>,
) -> jlong {
    let s = get_rust_string!(&mut env, s, || 0);
    let d = match Decimal::from_str(&s) {
        Ok(d) => d,
        Err(e) => return SurrealError::SurrealDBJni(e.to_string()).exception(&mut env, || 0),
    };
    let value = Value::Number(Number::Decimal(d));
    JniTypes::new_value_mut(value)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_ValueMut_newDuration<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
    millis: jlong,
) -> jlong {
    let value = Value::Duration(Duration::from_millis(millis as u64));
    JniTypes::new_value_mut(value)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_ValueMut_newDatetime<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    seconds: jlong,
    nanos: jlong,
) -> jlong {
    if let Some(d) = DateTime::from_timestamp(seconds, nanos as u32) {
        let value = Value::Datetime(Datetime::from(d));
        JniTypes::new_value_mut(value)
    } else {
        SurrealError::SurrealDBJni(format!(
            "Can't create the Datetime from seconds: {seconds}, nanos: {nanos}"
        ))
        .exception(&mut env, || 0)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_ValueMut_newId<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    let value = get_value_instance!(&mut env, ptr, || 0);
    if let Value::RecordId(record_id) = value.as_ref() {
        // Extract just the key part from the RecordId, not the whole RecordId with empty table
        let key_value = match &record_id.key {
            surrealdb::types::RecordIdKey::Number(n) => Value::Number(Number::Int(*n)),
            surrealdb::types::RecordIdKey::String(s) => Value::String(s.clone()),
            surrealdb::types::RecordIdKey::Uuid(u) => Value::Uuid(*u),
            surrealdb::types::RecordIdKey::Array(a) => Value::Array(a.clone()),
            surrealdb::types::RecordIdKey::Object(o) => Value::Object(o.clone()),
            surrealdb::types::RecordIdKey::Range(_) => {
                return SurrealError::SurrealDBJni(
                    "Range-based IDs are not supported for Id serialization".to_string()
                ).exception(&mut env, || 0);
            }
        };
        return JniTypes::new_value_mut(key_value);
    }
    SurrealError::NullPointerException("ID").exception(&mut env, || 0)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_ValueMut_newUuid<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    s: JString<'local>,
) -> jlong {
    let s = get_rust_string!(&mut env, s, || 0);
    if let Ok(uuid) = Uuid::from_str(&s) {
        let value = Value::Uuid(uuid);
        return create_instance(value, JniTypes::ValueMut);
    }
    SurrealError::NullPointerException("Uuid").exception(&mut env, || 0)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_ValueMut_newThing<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    let value = get_value_instance!(&mut env, ptr, || 0);
    if matches!(value.as_ref(), Value::RecordId(_)) {
        return JniTypes::new_value_mut(value.as_ref().clone());
    }
    SurrealError::NullPointerException("Thing").exception(&mut env, || 0)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_ValueMut_newArray<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    let value = get_value_instance!(&mut env, ptr, || 0);
    if matches!(value.as_ref(), Value::Array(_)) {
        return JniTypes::new_value_mut(value.as_ref().clone());
    }
    SurrealError::NullPointerException("Array").exception(&mut env, || 0)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_ValueMut_newArrayOf<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptrs: JLongArray<'local>,
) -> jlong {
    let ptrs = get_long_array!(&mut env, &ptrs, || 0);
    let mut values = Vec::with_capacity(ptrs.len());
    for ptr in ptrs {
        let value = take_value_mut_instance!(&mut env, ptr, || 0);
        values.push(value);
    }
    let value = Value::Array(Array::from(values));
    JniTypes::new_value_mut(value)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_ValueMut_newObject<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    let value = get_value_instance!(&mut env, ptr, || 0);
    if matches!(value.as_ref(), Value::Object(_)) {
        return JniTypes::new_value_mut(value.as_ref().clone());
    }
    SurrealError::NullPointerException("Object").exception(&mut env, || 0)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_ValueMut_newObjectOf<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptrs: JLongArray<'local>,
) -> jlong {
    let ptrs = get_long_array!(&mut env, &ptrs, || 0);
    let mut map = BTreeMap::new();
    for ptr in ptrs {
        let (key, value) = take_entry_mut_instance!(&mut env, ptr, || 0);
        map.insert(key, value);
    }
    let value = Value::Object(Object::from(map));
    create_instance(value, JniTypes::ValueMut)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_ValueMut_toString<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    let value = get_value_mut_instance!(&mut env, ptr, null_mut);
    new_string!(&mut env, value.to_sql(), null_mut)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_ValueMut_hashCode<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jint {
    let value = get_value_mut_instance!(&mut env, ptr, || 0);
    let mut hasher = DefaultHasher::new();
    value.hash(&mut hasher);
    let hash64 = hasher.finish();
    (hash64 & 0xFFFFFFFF) as jint
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_ValueMut_equals<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr1: jlong,
    ptr2: jlong,
) -> jboolean {
    let v1 = get_value_mut_instance!(&mut env, ptr1, || false as jboolean);
    let v2 = get_value_mut_instance!(&mut env, ptr2, || false as jboolean);
    v1.eq(v2) as jboolean
}
