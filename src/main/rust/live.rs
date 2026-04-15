use std::sync::Arc;

use jni::objects::{JObject, JValue};
use jni::sys::{jlong, jobject};
use jni::JNIEnv;

use crate::error::SurrealError;
use crate::{get_instance, new_string, take_instance, JniTypes, LiveStreamChannel, TOKIO_RUNTIME};

/// JNI implementation of `LiveStream.nextNative(long handle)`.
///
/// Blocks the calling thread until a live-query notification arrives or the
/// stream ends.  Returns a `LiveNotification` jobject, or `null` when the
/// stream has been closed (by `releaseNative` or because the server ended the
/// live query).
///
/// ## Locking protocol
///
/// 1. Acquire `recv_mutex` — serializes concurrent `nextNative` calls and
///    lets `releaseNative` know when no thread is inside `recv()`.
/// 2. Acquire `rx_mux` — borrows the channel receiver for the blocking call.
/// 3. Call `block_on(rx_ref.recv())` — parks the thread until a message
///    arrives or all senders are dropped (channel closed).
///
/// Both guards are held for the duration of the `recv()`.  This is safe
/// because `releaseNative` only acquires these locks *after* the channel has
/// been closed, guaranteeing `recv()` will have already returned.
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
        None => return JObject::null().into_raw(),
    };
    let item = match TOKIO_RUNTIME.block_on(rx_ref.recv()) {
        Ok(item) => item,
        Err(_) => return JObject::null().into_raw(),
    };
    let notification = match item {
        Ok(n) => n,
        Err(e) => return SurrealError::from(e).exception(&mut env, || std::ptr::null_mut()),
    };
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

/// JNI implementation of `LiveStream.releaseNative(long handle)`.
///
/// Shuts down the live query: stops the background thread, waits for any
/// in-progress `nextNative` call to finish, then frees the native handle.
///
/// ## Shutdown sequence
///
/// 1. **Drop `shutdown_tx`** — the background thread's `tokio::select!` loop
///    detects the closed shutdown channel and breaks.  This also causes the
///    background thread to drop its `tx_thread` sender, closing the
///    notification channel.
/// 2. **Join the background thread** — ensures `tx_thread` has been dropped
///    and the channel is fully closed before proceeding.
/// 3. **Acquire `recv_mutex`** — at this point the channel is closed, so any
///    `nextNative` call blocked on `recv()` has already returned and released
///    the mutex.  Acquiring it here is a final safety barrier.
/// 4. **Take the receiver** — drops it while holding `recv_mutex`, preventing
///    any new `recv()` attempt.
/// 5. **`take_instance`** — reclaims the boxed `LiveStreamChannel`, freeing
///    the allocation.  The Java side zeroes its `handle` field after this
///    call returns, preventing further native access.
#[no_mangle]
pub extern "system" fn Java_com_surrealdb_LiveStream_releaseNative<'local>(
    _env: JNIEnv<'local>,
    _class: jni::objects::JClass<'local>,
    handle_ptr: jlong,
) {
    if handle_ptr == 0 {
        return;
    }
    let channel_ref = match get_instance::<LiveStreamChannel>(handle_ptr, JniTypes::LiveStream) {
        Ok(r) => r,
        Err(_) => return,
    };
    let (recv_mutex, join_handle_mux, shutdown_tx_mux, rx_mux) = channel_ref;
    drop(shutdown_tx_mux.lock().take());
    if let Some(join_handle) = join_handle_mux.lock().take() {
        let _ = join_handle.join();
    }
    let _recv_guard = recv_mutex.lock();
    let _rx = rx_mux.lock().take();
    drop(_recv_guard);
    let _ = take_instance::<LiveStreamChannel>(handle_ptr, JniTypes::LiveStream);
}
