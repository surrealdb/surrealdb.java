use std::sync::Arc;
use std::time::Duration;

use jni::objects::JClass;
use jni::sys::jlong;
use jni::JNIEnv;
use parking_lot::Mutex;
use surrealdb::method::QueryStream;
use surrealdb::types::Value;
use tokio_stream::StreamExt;
use crate::{
    error::SurrealError, get_live_query_stream_instance, JniTypes, TOKIO_RUNTIME,
};

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_LiveQueryStream_deleteInstance<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) {
    if ptr == 0 {
        return;
    }
    #[cfg(debug_assertions)]
    crate::ALLOCATOR.remove(&ptr);

    // Drop inside the Tokio runtime so kill() can spawn without panicking
    let stream = unsafe { Box::from_raw(ptr as *mut Arc<Mutex<QueryStream<Value>>>) };
    TOKIO_RUNTIME.block_on(async {
        drop(stream);
    });
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_LiveQueryStream_next<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    let stream = get_live_query_stream_instance!(&mut env, ptr, || 0);
    let mut stream = stream.lock();
    let next = TOKIO_RUNTIME.block_on(async { stream.next().await });
    match next {
        Some(Ok(notification)) => JniTypes::new_notification(notification),
        Some(Err(err)) => SurrealError::from(err).exception(&mut env, || 0),
        None => 0,
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_LiveQueryStream_pollNext<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    timeout_millis: jlong,
) -> jlong {
    let stream = get_live_query_stream_instance!(&mut env, ptr, || 0);
    let mut stream = stream.lock();
    let millis = if timeout_millis < 0 { 0 } else { timeout_millis as u64 };
    let duration = Duration::from_millis(millis);
    let result = TOKIO_RUNTIME.block_on(async {
        tokio::time::timeout(duration, stream.next()).await
    });
    match result {
        Ok(Some(Ok(notification))) => JniTypes::new_notification(notification),
        Ok(Some(Err(err))) => SurrealError::from(err).exception(&mut env, || 0),
        Ok(None) => 0,
        Err(_) => 0,
    }
}
