use jni::objects::JClass;
use jni::sys::{jboolean, jlong};
use jni::JNIEnv;

use crate::error::SurrealError;
use crate::{get_value_iterator_instance, get_value_iterator_mut_instance, JniTypes};

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_ValueIterator_hasNext<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    let iter = get_value_iterator_instance!(&mut env, ptr, || false as jboolean);
    (iter.len() > 0) as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_ValueIterator_next<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    let iter = get_value_iterator_mut_instance!(&mut env, ptr, || 0);
    if let Some(v) = iter.next() {
        JniTypes::new_value(v.into())
    } else {
        SurrealError::NullPointerException("Value").exception(&mut env, || 0)
    }
}
