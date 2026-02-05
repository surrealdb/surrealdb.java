//! JNI bindings for client-side Transaction (begin/commit/cancel/query).

use std::sync::Arc;

use crate::error::SurrealError;
use crate::{
    check_query_result, get_rust_string, get_transaction_ref, release_instance, take_instance,
    JniTypes, TOKIO_RUNTIME,
};
use jni::objects::{JClass, JString};
use jni::sys::{jboolean, jlong};
use jni::JNIEnv;
use parking_lot::Mutex;
use surrealdb::engine::any::Any;
use surrealdb::method::Transaction;

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
    let query_str = get_rust_string!(&mut env, query, || 0);
    let res = TOKIO_RUNTIME.block_on(async { txn.query(&query_str).await });
    let res = check_query_result!(&mut env, res, || 0);
    JniTypes::new_response(Arc::new(Mutex::new(res)))
}
