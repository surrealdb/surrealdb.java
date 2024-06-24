use std::hash::{DefaultHasher, Hash, Hasher};
use std::ptr::null_mut;
use std::sync::Arc;

use jni::objects::{JClass, JString};
use jni::sys::{jboolean, jint, jlong, jstring};
use jni::JNIEnv;
use surrealdb::sql::{Id, Thing, Value};

use crate::error::SurrealError;
use crate::{create_instance, get_rust_string, get_value_instance, new_string};

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Thing_newTableId<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    table: JString<'local>,
    id: jlong,
) -> jlong {
    let table = get_rust_string!(&mut env, table, || 0);
    let value = Value::Thing(Thing::from((table, Id::Number(id))));
    create_instance(Arc::new(value))
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Thing_equals<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr1: jlong,
    ptr2: jlong,
) -> jboolean {
    let v1 = get_value_instance!(&mut env, ptr1, || false as jboolean);
    let v2 = get_value_instance!(&mut env, ptr2, || false as jboolean);
    v1.equal(v2.as_ref()) as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Thing_hashCode<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jint {
    let value = get_value_instance!(&mut env, ptr, || 0);
    let mut hasher = DefaultHasher::new();
    value.hash(&mut hasher);
    let hash64 = hasher.finish();
    (hash64 & 0xFFFFFFFF) as jint
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Thing_getTable<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    let value = get_value_instance!(&mut env, ptr, null_mut);
    if let Value::Thing(o) = value.as_ref() {
        new_string!(&mut env, &o.tb, null_mut)
    } else {
        SurrealError::NullPointerException("Thing").exception(&mut env, null_mut)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Thing_getId<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    let value = get_value_instance!(&mut env, ptr, || 0);
    if let Value::Thing(_) = value.as_ref() {
        create_instance(value)
    } else {
        SurrealError::NullPointerException("Thing").exception(&mut env, || 0)
    }
}
