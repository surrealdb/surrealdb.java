use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::jlong;
use surrealdb::sql::{Strand, Value};

use crate::{create_instance, get_rust_string};
use crate::error::SurrealError;

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_ValueMut_newString<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    s: JString<'local>,
) -> jlong {
    let s = get_rust_string!(&mut env, s, || 0);
    let value = Value::Strand(Strand(s));
    create_instance(value)
}