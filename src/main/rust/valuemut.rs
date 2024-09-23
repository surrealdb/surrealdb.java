use std::collections::BTreeMap;
use std::hash::{DefaultHasher, Hash, Hasher};
use std::ptr::null_mut;
use std::str::FromStr;

use chrono::DateTime;
use jni::objects::{JClass, JLongArray, JString};
use jni::sys::{jboolean, jdouble, jint, jlong, jstring};
use jni::JNIEnv;
use rust_decimal::Decimal;
use surrealdb::sql::{Array, Datetime, Duration, Number, Object, Strand, Value};

use crate::error::SurrealError;
use crate::{
    create_instance, get_long_array, get_rust_string, get_value_mut_instance, new_string,
    take_entry_mut_instance, take_value_mut_instance,
};

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_ValueMut_newString<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    s: JString<'local>,
) -> jlong {
    let s = get_rust_string!(&mut env, s, || 0);
    let value = Value::Strand(Strand::from(s));
    create_instance(value)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_ValueMut_newLong<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
    l: jlong,
) -> jlong {
    let value = Value::Number(Number::Int(l));
    create_instance(value)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_ValueMut_newDouble<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
    d: jdouble,
) -> jlong {
    let value = Value::Number(Number::Float(d));
    create_instance(value)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_ValueMut_newBoolean<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
    b: jboolean,
) -> jlong {
    let value = Value::Bool(b == 1);
    create_instance(value)
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
    create_instance(value)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_ValueMut_newDuration<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
    millis: jlong,
) -> jlong {
    let value = Value::Duration(Duration::from_millis(millis as u64));
    create_instance(value)
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
        create_instance(value)
    } else {
        SurrealError::SurrealDBJni(format!(
            "Can't create the Datetime from seconds: {seconds}, nanos: {nanos}"
        ))
            .exception(&mut env, || 0)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_ValueMut_newArray<'local>(
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
    create_instance(value)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_ValueMut_newObject<'local>(
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
    create_instance(value)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_ValueMut_toString<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    let value = get_value_mut_instance!(&mut env, ptr, null_mut);
    new_string!(&mut env, value.to_string(), null_mut)
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
