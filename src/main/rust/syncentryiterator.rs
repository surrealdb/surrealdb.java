use std::sync::Arc;

use jni::objects::JClass;
use jni::sys::{jboolean, jlong};
use jni::JNIEnv;

use crate::error::SurrealError;
use crate::{get_sync_entry_iterator_instance, JniTypes};

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_SynchronizedEntryIterator_hasNext<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    let iter = get_sync_entry_iterator_instance!(&mut env, ptr, || false as jboolean);
    let iter = iter.lock();
    (iter.len() > 0) as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_SynchronizedEntryIterator_next<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    let iter = get_sync_entry_iterator_instance!(&mut env, ptr, || 0);
    let mut iter = iter.lock();
    if let Some((key, value)) = iter.next() {
        JniTypes::new_key_value(key, value.into())
    } else {
        SurrealError::NoSuchElementException.exception(&mut env, || 0)
    }
}
