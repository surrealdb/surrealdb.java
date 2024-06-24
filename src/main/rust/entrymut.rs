use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::jlong;

use crate::{create_instance, get_rust_string, take_value_instance};
use crate::error::SurrealError;

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_EntryMut_create<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    s: JString<'local>,
    v: jlong,
) -> jlong {
    let s = get_rust_string!(&mut env, s, || 0);
    let v = take_value_instance!(&mut env, v, || 0);
    create_instance((s, v))
}