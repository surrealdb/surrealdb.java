use std::hash::{DefaultHasher, Hash, Hasher};
use std::ptr::null_mut;

use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::{jboolean, jint, jlong, jstring};

use crate::{create_instance, get_entry_mut_instance, get_rust_string, new_string, take_value_mut_instance};
use crate::error::SurrealError;

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_EntryMut_create<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    s: JString<'local>,
    v: jlong,
) -> jlong {
    let s = get_rust_string!(&mut env, s, || 0);
    let v = take_value_mut_instance!(&mut env, v, || 0);
    create_instance((s, v))
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_EntryMut_toString<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    let (key, value) = get_entry_mut_instance!(&mut env, ptr, null_mut);
    new_string!(&mut env, format!("({key},{value})"), null_mut)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_EntryMut_hashCode<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jint {
    let (key, value) = get_entry_mut_instance!(&mut env, ptr, ||0);
    let mut hasher = DefaultHasher::new();
    key.hash(&mut hasher);
    value.hash(&mut hasher);
    let hash64 = hasher.finish();
    (hash64 & 0xFFFFFFFF) as jint
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_EntryMut_equals<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr1: jlong,
    ptr2: jlong,
) -> jboolean {
    let (key1, value1) = get_entry_mut_instance!(&mut env, ptr1, ||0);
    let (key2, value2) = get_entry_mut_instance!(&mut env, ptr2, ||0);
    (key1.eq(key2) && value1.eq(value2)) as jboolean
}