use std::hash::{DefaultHasher, Hash, Hasher};
use std::ptr::null_mut;
use std::sync::Arc;

use jni::JNIEnv;
use jni::objects::JClass;
use jni::sys::{jboolean, jint, jlong, jstring};

use crate::{create_instance, get_entry_instance, new_string};

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Entry_getKey<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    let (key, _) = get_entry_instance!(&mut env, ptr, null_mut);
    new_string!(&mut env, key, null_mut)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Entry_getValue<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    let (_, value) = get_entry_instance!(&mut env, ptr, || 0);
    create_instance(value.clone())
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Entry_toString<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    let (key, value) = get_entry_instance!(&mut env, ptr, null_mut);
    new_string!(&mut env, format!("({key},{value})"), null_mut)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Entry_hashCode<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jint {
    let (key, value) = get_entry_instance!(&mut env, ptr, ||0);
    let mut hasher = DefaultHasher::new();
    key.hash(&mut hasher);
    value.hash(&mut hasher);
    let hash64 = hasher.finish();
    (hash64 & 0xFFFFFFFF) as jint
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Entry_equals<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr1: jlong,
    ptr2: jlong,
) -> jboolean {
    let (key1, value1) = get_entry_instance!(&mut env, ptr1, ||0);
    let (key2, value2) = get_entry_instance!(&mut env, ptr2, ||0);
    (key1.eq(key2) && value1.eq(value2)) as jboolean
}
