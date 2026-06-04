use std::hash::{DefaultHasher, Hash, Hasher};
use std::ptr::null_mut;

use crate::error::SurrealError;
use crate::with_env_body;
use crate::{
    get_entry_mut_instance, get_rust_string, new_string, release_instance, take_value_mut_instance,
    JniTypes,
};
use jni::objects::{JClass, JString};
use jni::sys::{jboolean, jint, jlong, jstring};
use jni::EnvUnowned;
use surrealdb::types::{ToSql, Value};

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_EntryMut_deleteInstance<'local>(
    _env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) {
    release_instance::<(String, Value)>(ptr);
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_EntryMut_create<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    s: JString<'local>,
    v: jlong,
) -> jlong {
    with_env_body!(env, env, {
        let s = get_rust_string!(env, s, || 0);
        let v = take_value_mut_instance!(env, v, || 0);
        JniTypes::new_key_value_mut(s, v)
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_EntryMut_toString<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    with_env_body!(env, env, {
        let (key, value) = get_entry_mut_instance!(env, ptr, null_mut);
        new_string!(env, format!("({key},{})", value.to_sql()), null_mut)
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_EntryMut_hashCode<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jint {
    with_env_body!(env, env, {
        let (key, value) = get_entry_mut_instance!(env, ptr, || 0);
        let mut hasher = DefaultHasher::new();
        key.hash(&mut hasher);
        value.hash(&mut hasher);
        let hash64 = hasher.finish();
        (hash64 & 0xFFFFFFFF) as jint
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_EntryMut_equals<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr1: jlong,
    ptr2: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let (key1, value1) = get_entry_mut_instance!(env, ptr1, || false);
        let (key2, value2) = get_entry_mut_instance!(env, ptr2, || false);
        (key1.eq(key2) && value1.eq(value2)) as jboolean
    })
}
