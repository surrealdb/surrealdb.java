use std::hash::{DefaultHasher, Hash, Hasher};
use std::ptr::null_mut;

use crate::with_env_body;
use jni::objects::JClass;
use jni::sys::{jboolean, jdoubleArray, jint, jlong, jstring};
use jni::EnvUnowned;
use surrealdb::types::{Geometry, Value};

use crate::error::SurrealError;
use crate::{get_value_instance, new_double_point, new_string};

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Geometry_isPoint<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || false as jboolean);
        if let Value::Geometry(g) = value.as_ref() {
            g.is_point() as jboolean
        } else {
            SurrealError::NullPointerException("Geometry").exception(env, || false as jboolean)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Geometry_getPoint<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jdoubleArray {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, null_mut);
        if let Value::Geometry(Geometry::Point(pt)) = value.as_ref() {
            return new_double_point!(env, pt, null_mut);
        }
        SurrealError::NullPointerException("Geometry").exception(env, null_mut)
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Geometry_equals<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr1: jlong,
    ptr2: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let v1 = get_value_instance!(env, ptr1, || false as jboolean);
        let v2 = get_value_instance!(env, ptr2, || false as jboolean);
        if let (Value::Geometry(g1), Value::Geometry(g2)) = (v1.as_ref(), v2.as_ref()) {
            return g1.eq(g2) as jboolean;
        }
        SurrealError::NullPointerException("Geometry").exception(env, || false as jboolean)
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Geometry_hashCode<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jint {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || 0);
        if let Value::Geometry(g) = value.as_ref() {
            let mut hasher = DefaultHasher::new();
            g.hash(&mut hasher);
            let hash64 = hasher.finish();
            return (hash64 & 0xFFFFFFFF) as jint;
        }
        SurrealError::NullPointerException("Geometry").exception(env, || 0)
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Geometry_toString<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, null_mut);
        if let Value::Geometry(g) = value.as_ref() {
            return new_string!(env, g.to_string(), null_mut);
        }
        SurrealError::NullPointerException("Geometry").exception(env, null_mut)
    })
}
