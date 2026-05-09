//! JNI bindings for client-side Transaction (begin/commit/cancel/query).

use std::collections::BTreeMap;
use std::sync::Arc;

use crate::error::SurrealError;
use crate::{
    check_query_result, get_long_array, get_rust_string, get_rust_string_array,
    get_transaction_ref, get_value_mut_instance, release_instance, take_instance, JniTypes,
    TOKIO_RUNTIME,
};
use jni::objects::{JClass, JLongArray, JObjectArray, JString};
use jni::sys::{jboolean, jlong};
use jni::JNIEnv;
use parking_lot::Mutex;
use surrealdb::engine::any::Any;
use surrealdb::method::Transaction;
use surrealdb::types::Value;

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Transaction_nativeDeleteInstance<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) {
    release_instance::<Transaction<Any>>(ptr);
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Transaction_commit<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    // take_instance must be first: Transaction::commit(self) takes ownership. On commit failure we drop the txn (rollback).
    let txn = match take_instance::<Transaction<Any>>(ptr, JniTypes::Transaction) {
        Ok(t) => t,
        Err(e) => return e.exception(&mut env, || false as jboolean),
    };
    if let Err(e) = TOKIO_RUNTIME.block_on(async { txn.commit().await }) {
        return SurrealError::from(e).exception(&mut env, || false as jboolean);
    }
    true as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Transaction_cancel<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    // take_instance must be first: Transaction::cancel(self) takes ownership. On cancel failure we drop the txn.
    let txn = match take_instance::<Transaction<Any>>(ptr, JniTypes::Transaction) {
        Ok(t) => t,
        Err(e) => return e.exception(&mut env, || false as jboolean),
    };
    if let Err(e) = TOKIO_RUNTIME.block_on(async { txn.cancel().await }) {
        return SurrealError::from(e).exception(&mut env, || false as jboolean);
    }
    true as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Transaction_query<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    query: JString<'local>,
) -> jlong {
    let txn = get_transaction_ref!(&mut env, ptr, || 0);
    let query = get_rust_string!(&mut env, query, || 0);
    let res = TOKIO_RUNTIME.block_on(async { txn.query(&query).await });
    let res = check_query_result!(&mut env, res, || 0);
    JniTypes::new_response(Arc::new(Mutex::new(res)))
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Transaction_queryWithBindings<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    query: JString<'local>,
    params_keys: JObjectArray<'local>,
    params_values: JLongArray<'local>,
) -> jlong {
    let txn = get_transaction_ref!(&mut env, ptr, || 0);
    let query = get_rust_string!(&mut env, query, || 0);
    let keys = get_rust_string_array!(&mut env, params_keys, || 0);
    let value_ptrs = get_long_array!(&mut env, &params_values, || 0);
    let mut params_map = BTreeMap::<String, Value>::new();
    for (key, value_ptr) in keys.into_iter().zip(value_ptrs) {
        let value = get_value_mut_instance!(&mut env, value_ptr, || 0);
        params_map.insert(key, value.clone());
    }

    let res = TOKIO_RUNTIME.block_on(async { txn.query(&query).bind(params_map).await });
    let res = check_query_result!(&mut env, res, || 0);
    JniTypes::new_response(Arc::new(Mutex::new(res)))
}
