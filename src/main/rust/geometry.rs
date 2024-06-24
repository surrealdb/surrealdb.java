use std::ptr::null_mut;
use std::sync::Arc;

use jni::objects::JClass;
use jni::sys::{jboolean, jdoubleArray, jlong};
use jni::JNIEnv;
use surrealdb::sql::{Geometry, Value};

use crate::error::SurrealError;
use crate::{get_value_instance, new_double_point};

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Geometry_isPoint<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    let value = get_value_instance!(&mut env, ptr, || false as jboolean);
    if let Value::Geometry(g) = value.as_ref() {
        g.is_point() as jboolean
    } else {
        SurrealError::NullPointerException("Thing").exception(&mut env, || false as jboolean)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Geometry_getPoint<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jdoubleArray {
    let value = get_value_instance!(&mut env, ptr, null_mut);
    if let Value::Geometry(Geometry::Point(pt)) = value.as_ref() {
        return new_double_point!(&mut env, pt, null_mut);
    }
    SurrealError::NullPointerException("Geometry/Point").exception(&mut env, null_mut)
}
