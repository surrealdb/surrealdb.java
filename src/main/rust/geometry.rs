use std::hash::{DefaultHasher, Hash, Hasher};
use std::ptr::null_mut;
use std::sync::Arc;

use jni::JNIEnv;
use jni::objects::JClass;
use jni::sys::{jboolean, jdoubleArray, jint, jlong, jstring};
use surrealdb::sql::{Geometry, Value};

use crate::{get_value_instance, new_double_point, new_string};
use crate::error::SurrealError;

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

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Geometry_equals<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr1: jlong,
    ptr2: jlong,
) -> jboolean {
    let v1 = get_value_instance!(&mut env, ptr1, || false as jboolean);
    let v2 = get_value_instance!(&mut env, ptr2, || false as jboolean);
    if let
        (Value::Geometry(g1), Value::Geometry(g2)) = (v1.as_ref(), v2.as_ref()) {
        return g1.eq(g2) as jboolean;
    }
    SurrealError::NullPointerException("Geometry").exception(&mut env, || false as jboolean)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Geometry_hashCode<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jint {
    let value = get_value_instance!(&mut env, ptr, || 0);
    if let Value::Geometry(g) = value.as_ref() {
        let mut hasher = DefaultHasher::new();
        g.hash(&mut hasher);
        let hash64 = hasher.finish();
        return (hash64 & 0xFFFFFFFF) as jint;
    }
    SurrealError::NullPointerException("Geometry").exception(&mut env, || 0)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Geometry_toString<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    let value = get_value_instance!(&mut env, ptr, null_mut);
    if let Value::Geometry(g) = value.as_ref() {
        new_string!(&mut env, g.to_string(), null_mut)
    }
    SurrealError::NullPointerException("Geometry").exception(&mut env, null_mut)
}