use std::sync::Arc;

use jni::objects::JClass;
use jni::sys::{jboolean, jlong};
use jni::JNIEnv;

use crate::error::SurrealError;
use crate::{create_instance, get_sync_value_iterator_instance};

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_SynchronizedValueIterator_hasNext<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    let iter = get_sync_value_iterator_instance!(&mut env, ptr, || false as jboolean);
    let iter = iter.lock();
    (iter.len() > 0) as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_SynchronizedValueIterator_next<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    let iter = get_sync_value_iterator_instance!(&mut env, ptr, || 0);
    let mut iter = iter.lock();
    if let Some(v) = iter.next() {
        create_instance(Arc::new(v))
    } else {
        SurrealError::NoSuchElementException.exception(&mut env, || 0)
    }
}
