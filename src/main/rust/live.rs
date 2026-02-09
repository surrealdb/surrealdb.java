use std::sync::Arc;

use jni::objects::{JObject, JValue};
use jni::sys::{jlong, jobject};
use jni::JNIEnv;

use crate::error::SurrealError;
use crate::{get_instance, new_string, release_instance, JniTypes, LiveNotificationResult, TOKIO_RUNTIME};

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_LiveStream_nextNative<'local>(
    mut env: JNIEnv<'local>,
    _class: jni::objects::JClass<'local>,
    handle_ptr: jlong,
) -> jobject {
    let rx = match get_instance::<async_channel::Receiver<LiveNotificationResult>>(
        handle_ptr,
        JniTypes::LiveStream,
    ) {
        Ok(r) => r,
        Err(e) => return e.exception(&mut env, || std::ptr::null_mut()),
    };
    let item = match TOKIO_RUNTIME.block_on(rx.recv()) {
        Ok(item) => item,
        Err(_) => return JObject::null().into_raw(), // channel closed
    };
    let notification = match item {
        Ok(n) => n,
        Err(e) => return SurrealError::from(e).exception(&mut env, || std::ptr::null_mut()),
    };
    // Build Java LiveNotification(action, valuePtr, queryId)
    let action_raw = new_string!(&mut env, notification.action.to_string(), || std::ptr::null_mut());
    let action_str = unsafe { JObject::from_raw(action_raw) };
    let value_ptr = JniTypes::new_value(Arc::new(notification.data));
    let query_id_raw = new_string!(
        &mut env,
        notification.query_id.to_string(),
        || std::ptr::null_mut()
    );
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
    release_instance::<async_channel::Receiver<LiveNotificationResult>>(handle_ptr);
}
