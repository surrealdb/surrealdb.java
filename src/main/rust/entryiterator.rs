use std::iter::Iterator;
use std::sync::Arc;

use jni::JNIEnv;
use jni::objects::JClass;
use jni::sys::{jboolean, jlong};

use crate::{create_instance, get_entry_iterator_instance, get_entry_iterator_mut_instance};
use crate::error::SurrealError;

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_EntryIterator_hasNext<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    id: jlong,
) -> jboolean {
    let iter = get_entry_iterator_instance!(&mut env, id, ||false as jboolean);
    (iter.len() > 0) as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_EntryIterator_next<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    id: jlong,
) -> jlong {
    let iter = get_entry_iterator_mut_instance!(&mut env, id, ||0);
    if let Some((k, v)) = iter.next() {
        create_instance((k, Arc::new(v)))
    } else {
        SurrealError::NullPointerException("Value").exception(&mut env, || 0)
    }
}