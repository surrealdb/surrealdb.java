use std::sync::Arc;

use jni::objects::JClass;
use jni::sys::{jint, jlong, jstring};
use jni::JNIEnv;
use surrealdb::types::Action;

use crate::{get_notification_instance, new_string, release_instance, JniTypes};

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Notification_deleteInstance<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) {
    release_instance::<surrealdb::Notification<surrealdb::types::Value>>(ptr);
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Notification_getQueryId<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    let notification = get_notification_instance!(&mut env, ptr, || std::ptr::null_mut());
    new_string!(&mut env, notification.query_id.to_string(), || std::ptr::null_mut())
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Notification_getActionCode<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jint {
    let notification = get_notification_instance!(&mut env, ptr, || 0);
    match notification.action {
        Action::Create => 0,
        Action::Update => 1,
        Action::Delete => 2,
        Action::Killed => 3,
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Notification_getData<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    let notification = get_notification_instance!(&mut env, ptr, || 0);
    let value = notification.data.clone();
    JniTypes::new_value(Arc::new(value))
}
