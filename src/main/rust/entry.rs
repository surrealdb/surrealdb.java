use std::hash::{DefaultHasher, Hash, Hasher};
use std::ptr::null_mut;

use crate::with_env_body;
use crate::{get_entry_instance, new_string, JniTypes};
use jni::objects::JClass;
use jni::sys::{jboolean, jint, jlong, jstring};
use jni::EnvUnowned;
use surrealdb::types::ToSql;

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Entry_getKey<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    with_env_body!(env, env, {
        let (key, _) = get_entry_instance!(env, ptr, null_mut);
        new_string!(env, key, null_mut)
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Entry_getValue<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        let (_, value) = get_entry_instance!(env, ptr, || 0);
        JniTypes::new_value(value.clone())
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Entry_toString<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    with_env_body!(env, env, {
        let (key, value) = get_entry_instance!(env, ptr, null_mut);
        new_string!(env, format!("({key},{})", value.to_sql()), null_mut)
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Entry_hashCode<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jint {
    with_env_body!(env, env, {
        let (key, value) = get_entry_instance!(env, ptr, || 0);
        let mut hasher = DefaultHasher::new();
        key.hash(&mut hasher);
        value.hash(&mut hasher);
        let hash64 = hasher.finish();
        (hash64 & 0xFFFFFFFF) as jint
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Entry_equals<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr1: jlong,
    ptr2: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let (key1, value1) = get_entry_instance!(env, ptr1, || false);
        let (key2, value2) = get_entry_instance!(env, ptr2, || false);
        (key1.eq(key2) && value1.eq(value2)) as jboolean
    })
}
