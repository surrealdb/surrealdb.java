//! JNI bindings for client-side Transaction (begin/commit/cancel/query).

use std::sync::Arc;

use crate::error::SurrealError;
use crate::with_env_body;
use crate::{
    build_params_map, check_query_result, get_rust_string, get_transaction_ref, release_instance,
    take_instance, JniTypes, TOKIO_RUNTIME,
};
use jni::objects::{JClass, JLongArray, JObjectArray, JString};
use jni::sys::{jboolean, jlong};
use jni::EnvUnowned;
use parking_lot::Mutex;
use surrealdb::engine::any::Any;
use surrealdb::method::Transaction;

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Transaction_nativeDeleteInstance<'local>(
    _env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) {
    release_instance::<Transaction<Any>>(ptr);
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Transaction_commit<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        // take_instance must be first: Transaction::commit(self) takes ownership. On commit failure we drop the txn (rollback).
        let txn = match take_instance::<Transaction<Any>>(ptr, JniTypes::Transaction) {
            Ok(t) => t,
            Err(e) => return e.exception(env, || false as jboolean),
        };
        if let Err(e) = TOKIO_RUNTIME.block_on(async { txn.commit().await }) {
            return SurrealError::from(e).exception(env, || false as jboolean);
        }
        true as jboolean
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Transaction_cancel<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        // take_instance must be first: Transaction::cancel(self) takes ownership. On cancel failure we drop the txn.
        let txn = match take_instance::<Transaction<Any>>(ptr, JniTypes::Transaction) {
            Ok(t) => t,
            Err(e) => return e.exception(env, || false as jboolean),
        };
        if let Err(e) = TOKIO_RUNTIME.block_on(async { txn.cancel().await }) {
            return SurrealError::from(e).exception(env, || false as jboolean);
        }
        true as jboolean
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Transaction_query<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    query: JString<'local>,
) -> jlong {
    with_env_body!(env, env, {
        let txn = get_transaction_ref!(env, ptr, || 0);
        let query = get_rust_string!(env, query, || 0);
        let res = TOKIO_RUNTIME.block_on(async { txn.query(&query).await });
        let res = check_query_result!(env, res, || 0);
        JniTypes::new_response(Arc::new(Mutex::new(res)))
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Transaction_queryWithBindings<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    query: JString<'local>,
    params_keys: JObjectArray<'local, JString<'local>>,
    params_values: JLongArray<'local>,
) -> jlong {
    with_env_body!(env, env, {
        let txn = get_transaction_ref!(env, ptr, || 0);
        let query = get_rust_string!(env, query, || 0);
        let params_map = build_params_map!(env, params_keys, params_values, || 0);
        let res = TOKIO_RUNTIME.block_on(async { txn.query(&query).bind(params_map).await });
        let res = check_query_result!(env, res, || 0);
        JniTypes::new_response(Arc::new(Mutex::new(res)))
    })
}
