use std::sync::Arc;

use jni::objects::{JObject, JValue};
use jni::sys::{jlong, jobject};
use jni::JNIEnv;

use crate::error::SurrealError;
use crate::{get_instance, new_string, take_instance, JniTypes, LiveStreamChannel, TOKIO_RUNTIME};

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_LiveStream_nextNative<'local>(
    mut env: JNIEnv<'local>,
    _class: jni::objects::JClass<'local>,
    handle_ptr: jlong,
) -> jobject {
    let (recv_mutex, _join_handle_mux, _shutdown_tx_mux, rx_mux) =
        match get_instance::<LiveStreamChannel>(handle_ptr, JniTypes::LiveStream) {
            Ok(r) => r,
            Err(e) => return e.exception(&mut env, || std::ptr::null_mut()),
        };
    let _recv_guard = recv_mutex.lock();
    let rx_opt_guard = rx_mux.lock();
    let rx_ref = match rx_opt_guard.as_ref() {
        Some(rx) => rx,
        None => return JObject::null().into_raw(), // already released
    };
    let item = match TOKIO_RUNTIME.block_on(rx_ref.recv()) {
        Ok(item) => item,
        Err(_) => return JObject::null().into_raw(), // channel closed
    };
    let notification = match item {
        Ok(n) => n,
        Err(e) => return SurrealError::from(e).exception(&mut env, || std::ptr::null_mut()),
    };
    // Build Java LiveNotification(action, valuePtr, queryId)
    let action_raw = new_string!(&mut env, notification.action.to_string(), || {
        std::ptr::null_mut()
    });
    let action_str = unsafe { JObject::from_raw(action_raw) };
    let value_ptr = JniTypes::new_value(Arc::new(notification.data));
    let query_id_raw = new_string!(&mut env, notification.query_id.to_string(), || {
        std::ptr::null_mut()
    });
    let query_id_str = unsafe { JObject::from_raw(query_id_raw) };
    let class = match env.find_class("com/surrealdb/LiveNotification") {
        Ok(c) => c,
        Err(e) => return SurrealError::from(e).exception(&mut env, || std::ptr::null_mut()),
    };
    let args = [
        JValue::Object(&action_str),
        JValue::Long(value_ptr),
        JValue::Object(&query_id_str),
    ];
    match env.new_object(class, "(Ljava/lang/String;JLjava/lang/String;)V", &args) {
        Ok(obj) => obj.into_raw(),
        Err(e) => SurrealError::from(e).exception(&mut env, || std::ptr::null_mut()),
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_LiveStream_releaseNative<'local>(
    _env: JNIEnv<'local>,
    _class: jni::objects::JClass<'local>,
    handle_ptr: jlong,
) {
    if handle_ptr == 0 {
        return;
    }
    // Do NOT take_instance yet: another thread may be in nextNative (get_instance + recv).
    // First close the channel via the stored sender, join the background thread, then acquire
    // the recv mutex (so no thread is in recv()), then take_instance and drop the receiver.
    let channel_ref = match get_instance::<LiveStreamChannel>(handle_ptr, JniTypes::LiveStream) {
        Ok(r) => r,
        Err(_) => return,
    };
    let (recv_mutex, join_handle_mux, shutdown_tx_mux, rx_mux) = channel_ref;
    drop(shutdown_tx_mux.lock().take()); // drop sender so background thread exits and channel closes
    if let Some(join_handle) = join_handle_mux.lock().take() {
        let _ = join_handle.join();
    }
    let _recv_guard = recv_mutex.lock(); // wait until any thread in nextNative has left recv()
    let _rx = rx_mux.lock().take(); // take and drop receiver while holding recv_guard
    drop(_recv_guard);
    let _ = take_instance::<LiveStreamChannel>(handle_ptr, JniTypes::LiveStream);
    // free the box
}
