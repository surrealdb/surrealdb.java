use std::ptr::null_mut;
use std::sync::Arc;

use jni::objects::JClass;
use jni::sys::{jlong, jstring};
use jni::JNIEnv;

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
