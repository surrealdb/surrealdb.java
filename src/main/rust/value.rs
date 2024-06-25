use std::hash::{DefaultHasher, Hash, Hasher};
use std::ptr::null_mut;
use std::sync::Arc;

use jni::objects::JClass;
use jni::sys::{jboolean, jdouble, jint, jlong, jlongArray, jstring};
use jni::JNIEnv;
use surrealdb::sql::{Number, Value};

use crate::error::SurrealError;
use crate::{create_instance, get_value_instance, new_jlong_array, new_string, release_instance};

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_deleteInstance<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    release_instance::<Arc<Value>>(ptr);
    true as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isArray<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    let value = get_value_instance!(&mut env, ptr, || false as jboolean);
    value.is_array() as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_getArray<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    let value = get_value_instance!(&mut env, ptr, || 0);
    if value.is_array() {
        create_instance(value)
    } else {
        SurrealError::NullPointerException("Array").exception(&mut env, || 0)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isObject<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    let value = get_value_instance!(&mut env, ptr, || false as jboolean);
    value.is_object() as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_getObject<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    let value = get_value_instance!(&mut env, ptr, || 0);
    if value.is_object() {
        create_instance(value)
    } else {
        SurrealError::NullPointerException("Object").exception(&mut env, || 0)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isBoolean<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    let value = get_value_instance!(&mut env, ptr, || false as jboolean);
    value.is_bool() as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_getBoolean<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    let value = get_value_instance!(&mut env, ptr, || 0);
    if let Value::Bool(b) = value.as_ref() {
        *b as jboolean
    } else {
        SurrealError::NullPointerException("Boolean").exception(&mut env, || 0)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isBytes<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    let value = get_value_instance!(&mut env, ptr, || false as jboolean);
    value.is_bytes() as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isLong<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    let value = get_value_instance!(&mut env, ptr, || false as jboolean);
    value.is_int() as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_getLong<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    let value = get_value_instance!(&mut env, ptr, || 0);
    if let Value::Number(Number::Int(i)) = value.as_ref() {
        *i
    } else {
        SurrealError::NullPointerException("Long").exception(&mut env, || 0)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isDateTime<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    let value = get_value_instance!(&mut env, ptr, || false as jboolean);
    value.is_datetime() as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_getDateTime<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlongArray {
    let value = get_value_instance!(&mut env, ptr, null_mut);
    if let Value::Datetime(dt) = value.as_ref() {
        let seconds = dt.timestamp();
        let nanos = dt.timestamp_subsec_nanos() as i64;
        let buf: [jlong; 2] = [seconds, nanos];
        new_jlong_array!(&mut env, &buf, null_mut)
    } else {
        SurrealError::NullPointerException("Geometry").exception(&mut env, null_mut)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isDuration<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    let value = get_value_instance!(&mut env, ptr, || false as jboolean);
    value.is_duration() as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_getDuration<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    let value = get_value_instance!(&mut env, ptr, || 0);
    if let Value::Duration(d) = value.as_ref() {
        d.as_millis() as jlong
    } else {
        SurrealError::NullPointerException("Geometry").exception(&mut env, || 0)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isBigDecimal<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    let value = get_value_instance!(&mut env, ptr, || false as jboolean);
    value.is_decimal() as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_getBigDecimal<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    let value = get_value_instance!(&mut env, ptr, null_mut);
    if let Value::Number(Number::Decimal(d)) = value.as_ref() {
        new_string!(&mut env, &d.to_string(), null_mut)
    } else {
        SurrealError::NullPointerException("BigDecimal").exception(&mut env, null_mut)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isDouble<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    let value = get_value_instance!(&mut env, ptr, || false as jboolean);
    value.is_float() as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_getDouble<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jdouble {
    let value = get_value_instance!(&mut env, ptr, || 0.0);
    if let Value::Number(Number::Float(f)) = value.as_ref() {
        *f
    } else {
        SurrealError::NullPointerException("Double").exception(&mut env, || 0.0)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isGeometry<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    let value = get_value_instance!(&mut env, ptr, || false as jboolean);
    value.is_geometry() as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_getGeometry<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    let value = get_value_instance!(&mut env, ptr, || 0);
    if let Value::Geometry(_) = value.as_ref() {
        create_instance(value)
    } else {
        SurrealError::NullPointerException("Geometry").exception(&mut env, || 0)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isNone<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    let value = get_value_instance!(&mut env, ptr, || false as jboolean);
    value.is_none() as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isNull<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    let value = get_value_instance!(&mut env, ptr, || false as jboolean);
    value.is_null() as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isString<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    let value = get_value_instance!(&mut env, ptr, || false as jboolean);
    value.is_strand() as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_getString<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    let value = get_value_instance!(&mut env, ptr, null_mut);
    if let Value::Strand(s) = value.as_ref() {
        new_string!(&mut env, &s.0, null_mut)
    } else {
        SurrealError::NullPointerException("String").exception(&mut env, null_mut)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isThing<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    let value = get_value_instance!(&mut env, ptr, || false as jboolean);
    value.is_thing() as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_getThing<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    let value = get_value_instance!(&mut env, ptr, || 0);
    if let Value::Thing(_) = value.as_ref() {
        create_instance(value)
    } else {
        SurrealError::NullPointerException("Thing").exception(&mut env, || 0)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isUuid<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    let value = get_value_instance!(&mut env, ptr, || false as jboolean);
    value.is_uuid() as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_getUuid<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    let value = get_value_instance!(&mut env, ptr, null_mut);
    if let Value::Uuid(uuid) = value.as_ref() {
        new_string!(&mut env, uuid.0.to_string(), null_mut)
    } else {
        SurrealError::NullPointerException("UUID").exception(&mut env, null_mut)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_toString<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    let value = get_value_instance!(&mut env, ptr, null_mut);
    let s = value.to_string();
    new_string!(&mut env, s, null_mut)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_toPrettyString<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    let value = get_value_instance!(&mut env, ptr, null_mut);
    let s = format!("{value:#}");
    new_string!(&mut env, s, null_mut)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_hashCode<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jint {
    let value = get_value_instance!(&mut env, ptr, || 0);
    let mut hasher = DefaultHasher::new();
    value.hash(&mut hasher);
    let hash64 = hasher.finish();
    (hash64 & 0xFFFFFFFF) as jint
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_equals<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr1: jlong,
    ptr2: jlong,
) -> jboolean {
    let v1 = get_value_instance!(&mut env, ptr1, || false as jboolean);
    let v2 = get_value_instance!(&mut env, ptr2, || false as jboolean);
    return v1.equal(v2.as_ref()) as jboolean;
}
