use std::hash::{DefaultHasher, Hash, Hasher};
use std::ptr::null_mut;
use std::str::FromStr;

use jni::objects::{JClass, JString};
use jni::sys::{jboolean, jint, jlong, jstring};
use jni::JNIEnv;
use surrealdb::types::{RecordId, RecordIdKey, ToSql, Uuid, Value};

use crate::error::SurrealError;
use crate::{get_rust_string, get_value_instance, new_string, JniTypes};

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Id_newLongId<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
    id: jlong,
) -> jlong {
    let value = Value::RecordId(RecordId::new("", RecordIdKey::Number(id)));
    JniTypes::new_value(value.into())
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Id_newStringId<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    id: JString<'local>,
) -> jlong {
    let id = get_rust_string!(&mut env, id, || 0);
    let value = Value::RecordId(RecordId::new("", RecordIdKey::String(id)));
    JniTypes::new_value(value.into())
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Id_newUuidId<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    id: JString<'local>,
) -> jlong {
    let id = get_rust_string!(&mut env, id, || 0);
    if let Ok(uuid) = Uuid::from_str(&id) {
        let value = Value::RecordId(RecordId::new("", RecordIdKey::Uuid(uuid)));
        JniTypes::new_value(value.into())
    } else {
        SurrealError::NullPointerException("Thing").exception(&mut env, || 0)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Id_isLong<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    let value = get_value_instance!(&mut env, ptr, || false as jboolean);
    if let Value::RecordId(o) = value.as_ref() {
        if let RecordIdKey::Number(_) = &o.key {
            true as jboolean
        } else {
            false as jboolean
        }
    } else {
        SurrealError::NullPointerException("Id").exception(&mut env, || false as jboolean)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Id_getLong<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    let value = get_value_instance!(&mut env, ptr, || 0);
    if let Value::RecordId(o) = value.as_ref() {
        if let RecordIdKey::Number(i) = &o.key {
            return *i as jlong;
        }
    }
    SurrealError::NullPointerException("Id").exception(&mut env, || 0)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Id_isString<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    let value = get_value_instance!(&mut env, ptr, || false as jboolean);
    if let Value::RecordId(o) = value.as_ref() {
        if let RecordIdKey::String(_) = &o.key {
            true as jboolean
        } else {
            false as jboolean
        }
    } else {
        SurrealError::NullPointerException("Id").exception(&mut env, || false as jboolean)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Id_getString<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    let value = get_value_instance!(&mut env, ptr, null_mut);
    if let Value::RecordId(o) = value.as_ref() {
        if let RecordIdKey::String(s) = &o.key {
            return new_string!(&mut env, s, null_mut);
        }
    }
    SurrealError::NullPointerException("Id").exception(&mut env, null_mut)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Id_isUuid<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    let value = get_value_instance!(&mut env, ptr, || false as jboolean);
    if let Value::RecordId(o) = value.as_ref() {
        if let RecordIdKey::Uuid(_) = &o.key {
            true as jboolean
        } else {
            false as jboolean
        }
    } else {
        SurrealError::NullPointerException("Id").exception(&mut env, || false as jboolean)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Id_getUuid<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    let value = get_value_instance!(&mut env, ptr, null_mut);
    if let Value::RecordId(o) = value.as_ref() {
        if let RecordIdKey::Uuid(uuid) = &o.key {
            return new_string!(&mut env, uuid.0.to_string(), null_mut);
        }
    }
    SurrealError::NullPointerException("Id").exception(&mut env, null_mut)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Id_isObject<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    let value = get_value_instance!(&mut env, ptr, || false as jboolean);
    if let Value::RecordId(o) = value.as_ref() {
        if let RecordIdKey::Object(_) = &o.key {
            true as jboolean
        } else {
            false as jboolean
        }
    } else {
        SurrealError::NullPointerException("Id").exception(&mut env, || false as jboolean)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Id_getObject<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    let value = get_value_instance!(&mut env, ptr, || 0);
    if let Value::RecordId(o) = value.as_ref() {
        if let RecordIdKey::Object(o) = &o.key {
            //TODO avoid cloning?
            return JniTypes::new_value(Value::Object(o.clone()).into());
        }
    }
    SurrealError::NullPointerException("Id").exception(&mut env, || 0)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Id_isArray<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    let value = get_value_instance!(&mut env, ptr, || false as jboolean);
    if let Value::RecordId(o) = value.as_ref() {
        if let RecordIdKey::Array(_) = &o.key {
            true as jboolean
        } else {
            false as jboolean
        }
    } else {
        SurrealError::NullPointerException("Id").exception(&mut env, || false as jboolean)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Id_getArray<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    let value = get_value_instance!(&mut env, ptr, || 0);
    if let Value::RecordId(o) = value.as_ref() {
        if let RecordIdKey::Array(a) = &o.key {
            //TODO no clone?
            return JniTypes::new_value(Value::Array(a.clone()).into());
        }
    }
    SurrealError::NullPointerException("Id").exception(&mut env, || 0)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Id_toString<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    let value = get_value_instance!(&mut env, ptr, null_mut);
    if let Value::RecordId(o) = value.as_ref() {
        let s = o.key.to_sql();
        return new_string!(&mut env, s, null_mut);
    }
    SurrealError::NullPointerException("Id").exception(&mut env, null_mut)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Id_hashCode<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jint {
    let value = get_value_instance!(&mut env, ptr, || 0);
    if let Value::RecordId(o) = value.as_ref() {
        let mut hasher = DefaultHasher::new();
        o.key.hash(&mut hasher);
        let hash64 = hasher.finish();
        return (hash64 & 0xFFFFFFFF) as jint;
    }
    SurrealError::NullPointerException("Id").exception(&mut env, || 0)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Id_equals<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr1: jlong,
    ptr2: jlong,
) -> jboolean {
    let v1 = get_value_instance!(&mut env, ptr1, || false as jboolean);
    let v2 = get_value_instance!(&mut env, ptr2, || false as jboolean);
    if let (Value::RecordId(t1), Value::RecordId(t2)) = (v1.as_ref(), v2.as_ref()) {
        return t1.key.eq(&t2.key) as jboolean;
    }
    SurrealError::NullPointerException("Id").exception(&mut env, || false as jboolean)
}
