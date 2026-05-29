use crate::with_env_body;
use jni::objects::JClass;
use jni::sys::{jboolean, jlong};
use jni::EnvUnowned;

use crate::error::SurrealError;
use crate::{get_entry_iterator_instance, get_entry_iterator_mut_instance, JniTypes};

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_EntryIterator_hasNext<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let iter = get_entry_iterator_instance!(env, ptr, || false as jboolean);
        (iter.len() > 0) as jboolean
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_EntryIterator_next<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        let iter = get_entry_iterator_mut_instance!(env, ptr, || 0);
        if let Some((k, v)) = iter.next() {
            JniTypes::new_key_value(k, v.into())
        } else {
            SurrealError::NoSuchElementException.exception(env, || 0)
        }
    })
}
