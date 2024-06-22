use std::ptr::null_mut;
use std::sync::Arc;

use jni::JNIEnv;
use jni::objects::JClass;
use jni::sys::{jlong, jstring};
use surrealdb::sql::Value;

use crate::{create_instance, get_value_instance, new_string};
use crate::error::SurrealError;

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Thing_getTable<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    id: jlong,
) -> jstring {
    let value = get_value_instance!(&mut env, id, ||null_mut());
    if let Value::Thing(o) = value.as_ref() {
        new_string!(&mut env, &o.tb, ||null_mut())
    } else {
        SurrealError::NullPointerException("Thing").exception(&mut env, || null_mut())
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Thing_getId<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    id: jlong,
) -> jlong {
    let value = get_value_instance!(&mut env, id, ||0);
    if let Value::Thing(_) = value.as_ref() {
        create_instance(value)
    } else {
        SurrealError::NullPointerException("Thing").exception(&mut env, || 0)
    }
}