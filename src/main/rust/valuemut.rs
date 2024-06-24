use std::collections::BTreeMap;
use std::hash::{DefaultHasher, Hash, Hasher};
use std::ptr::null_mut;

use jni::errors::Error;
use jni::JNIEnv;
use jni::objects::{JClass, JLongArray, JString};
use jni::sys::{jboolean, jint, jlong, jstring};
use surrealdb::sql::{Array, Number, Object, Strand, Value};

use crate::{create_instance, get_rust_string, get_value_mut_instance, new_string, take_entry_mut_instance, take_value_mut_instance};
use crate::error::SurrealError;

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_ValueMut_newString<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    s: JString<'local>,
) -> jlong {
    let s = get_rust_string!(&mut env, s, || 0);
    let value = Value::Strand(Strand(s));
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
pub extern "system" fn Java_com_surrealdb_ValueMut_newBoolean<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
    b: jboolean,
) -> jlong {
    let value = Value::Bool(b == 1);
    create_instance(value)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_ValueMut_newArray<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptrs: JLongArray<'local>,
) -> jlong {
    let ptrs = match get_long_array(&mut env, &ptrs) {
        Ok(ptrs) => ptrs,
        Err(e) => return SurrealError::from(e).exception(&mut env, || 0),
    };
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
    let ptrs = match get_long_array(&mut env, &ptrs) {
        Ok(ptrs) => ptrs,
        Err(e) => return SurrealError::from(e).exception(&mut env, || 0),
    };
    let mut map = BTreeMap::new();
    for ptr in ptrs {
        let (key, value) = take_entry_mut_instance!(&mut env, ptr, || 0);
        map.insert(key, value);
    }
    let value = Value::Object(Object(map));
    create_instance(value)
}

fn get_long_array(env: &mut JNIEnv, ptrs: &JLongArray) -> Result<Vec<jlong>, Error> {
    let length = env.get_array_length(ptrs).unwrap();
    let mut long_ptrs: Vec<jlong> = vec![0; length as usize];
    env.get_long_array_region(ptrs, 0, &mut long_ptrs)?;
    Ok(long_ptrs)
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
    let value = get_value_mut_instance!(&mut env, ptr, ||0);
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