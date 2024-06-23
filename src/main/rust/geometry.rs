use std::ptr::null_mut;
use std::sync::Arc;

use jni::JNIEnv;
use jni::objects::JClass;
use jni::sys::{jboolean, jdoubleArray, jlong};
use surrealdb::sql::{Geometry, Value};

use crate::{get_value_instance, new_double_point};
use crate::error::SurrealError;

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Geometry_isPoint<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    id: jlong,
) -> jboolean {
    let value = get_value_instance!(&mut env, id, ||false as jboolean);
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
    id: jlong,
) -> jdoubleArray {
    let value = get_value_instance!(&mut env, id, ||null_mut());
    if let Value::Geometry(g) = value.as_ref() {
        if let Geometry::Point(pt) = &g {
            return new_double_point!(&mut env, pt, ||null_mut());
        }
    }
    SurrealError::NullPointerException("Geometry/Point").exception(&mut env, || null_mut())
}

