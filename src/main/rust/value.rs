use std::hash::{DefaultHasher, Hash, Hasher};
use std::ops::Bound;
use std::ptr::null_mut;
use std::sync::Arc;

use crate::with_env_body;
use jni::objects::{AsJArrayRaw, JClass};
use jni::sys::{jboolean, jbyteArray, jdouble, jint, jlong, jlongArray, jstring};
use jni::EnvUnowned;
use surrealdb::types::{Array, Number, NumberKind, ToSql, Value};

use crate::error::SurrealError;
use crate::{get_value_instance, new_jlong_array, new_string, JniTypes};

/// The exact value of an integer-kind number as an `i64`, or `None` when the number is not an
/// integer kind or does not fit (only a `u64` above `i64::MAX` can fail to fit).
fn number_as_long(n: &Number) -> Option<i64> {
    // Every variant is listed on purpose: a new `Number` variant must fail to compile here.
    match n {
        Number::Int(i) => Some(*i),
        Number::I8(i) => Some(i64::from(*i)),
        Number::U8(i) => Some(i64::from(*i)),
        Number::I16(i) => Some(i64::from(*i)),
        Number::U16(i) => Some(i64::from(*i)),
        Number::I32(i) => Some(i64::from(*i)),
        Number::U32(i) => Some(i64::from(*i)),
        Number::U64(u) => i64::try_from(*u).ok(),
        Number::Float(_)
        | Number::F16(_)
        | Number::F32(_)
        | Number::Decimal(_)
        | Number::BigDecimal(_) => None,
    }
}

/// The exact value of a float-kind number as an `f64` (`f16` and `f32` widen losslessly).
fn number_as_double(n: &Number) -> Option<f64> {
    match n {
        Number::Float(f) => Some(*f),
        Number::F16(f) => Some(f.to_f64()),
        Number::F32(f) => Some(f64::from(*f)),
        Number::Int(_)
        | Number::I8(_)
        | Number::U8(_)
        | Number::I16(_)
        | Number::U16(_)
        | Number::I32(_)
        | Number::U32(_)
        | Number::U64(_)
        | Number::Decimal(_)
        | Number::BigDecimal(_) => None,
    }
}

/// The decimal text of an integer-kind number, or `None` when the number is not an integer kind.
fn number_as_integer_string(n: &Number) -> Option<String> {
    match n {
        Number::U64(u) => Some(u.to_string()),
        other => number_as_long(other).map(|i| i.to_string()),
    }
}

/// The SurrealQL kind name of a number, as returned by `type::of`.
fn number_kind_name(n: &Number) -> &'static str {
    match n.number_kind() {
        NumberKind::I8 => "i8",
        NumberKind::U8 => "u8",
        NumberKind::I16 => "i16",
        NumberKind::U16 => "u16",
        NumberKind::I32 => "i32",
        NumberKind::U32 => "u32",
        NumberKind::Int => "int",
        NumberKind::U64 => "u64",
        NumberKind::F16 => "f16",
        NumberKind::F32 => "f32",
        NumberKind::Float => "float",
        NumberKind::Decimal => "decimal",
        NumberKind::BigDecimal => "bigdecimal",
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_deleteInstance<'local>(
    _env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    JniTypes::release_value(ptr);
    true as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isArray<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || false as jboolean);
        value.is_array() as jboolean
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_getArray<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || 0);
        if value.is_array() {
            JniTypes::new_value(value)
        } else {
            SurrealError::NullPointerException("Array").exception(env, || 0)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isObject<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || false as jboolean);
        value.is_object() as jboolean
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_getObject<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || 0);
        if value.is_object() {
            JniTypes::new_value(value)
        } else {
            SurrealError::NullPointerException("Object").exception(env, || 0)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isBoolean<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || false as jboolean);
        value.is_bool() as jboolean
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_getBoolean<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || false);
        if let Value::Bool(b) = value.as_ref() {
            *b as jboolean
        } else {
            SurrealError::NullPointerException("Boolean").exception(env, || false)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isBytes<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || false as jboolean);
        value.is_bytes() as jboolean
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_getBytes<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jbyteArray {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, null_mut);
        if let Value::Bytes(bytes) = value.as_ref() {
            match env.byte_array_from_slice(bytes) {
                Ok(a) => a.as_jarray_raw(),
                Err(_) => null_mut(),
            }
        } else {
            SurrealError::NullPointerException("Bytes").exception(env, null_mut)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isLong<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || false as jboolean);
        matches!(value.as_ref(), Value::Number(n) if number_as_long(n).is_some()) as jboolean
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isBigInteger<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || false as jboolean);
        // Only a u64 above i64::MAX is an integer that does not fit a Java long.
        matches!(value.as_ref(), Value::Number(Number::U64(u)) if i64::try_from(*u).is_err())
            as jboolean
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_getBigInteger<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, null_mut);
        let text = match value.as_ref() {
            Value::Number(n) => number_as_integer_string(n),
            _ => None,
        };
        match text {
            Some(text) => new_string!(env, &text, null_mut),
            None => SurrealError::NullPointerException("BigInteger").exception(env, null_mut),
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isNumber<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || false as jboolean);
        value.is_number() as jboolean
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_getNumberKind<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, null_mut);
        if let Value::Number(n) = value.as_ref() {
            new_string!(env, number_kind_name(n), null_mut)
        } else {
            SurrealError::NullPointerException("Number").exception(env, null_mut)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_getLong<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || 0);
        match value.as_ref() {
            Value::Number(n) => match number_as_long(n) {
                Some(i) => i,
                None if matches!(n, Number::U64(_)) => SurrealError::SurrealDBJni(format!(
                    "{} does not fit in a long, use getBigInteger()",
                    n.to_sql()
                ))
                .exception(env, || 0),
                None => SurrealError::NullPointerException("Long").exception(env, || 0),
            },
            _ => SurrealError::NullPointerException("Long").exception(env, || 0),
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isDateTime<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || false as jboolean);
        value.is_datetime() as jboolean
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_getDateTime<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlongArray {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, null_mut);
        if let Value::Datetime(dt) = value.as_ref() {
            let seconds = dt.timestamp();
            let nanos = dt.timestamp_subsec_nanos() as i64;
            let buf: [jlong; 2] = [seconds, nanos];
            new_jlong_array!(env, &buf, null_mut)
        } else {
            SurrealError::NullPointerException("DateTime").exception(env, null_mut)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isDuration<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || false as jboolean);
        value.is_duration() as jboolean
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_getDuration<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlongArray {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, null_mut);
        if let Value::Duration(d) = value.as_ref() {
            // java.time.Duration holds a signed 64-bit second count; anything larger
            // cannot be represented, so fail loudly instead of wrapping negative.
            let seconds = match jlong::try_from(d.secs()) {
                Ok(secs) => secs,
                Err(_) => {
                    return SurrealError::SurrealDBJni(format!(
                        "Duration of {} seconds is too large for java.time.Duration",
                        d.secs()
                    ))
                    .exception(env, null_mut)
                }
            };
            let buf: [jlong; 2] = [seconds, d.subsec_nanos() as jlong];
            new_jlong_array!(env, &buf, null_mut)
        } else {
            SurrealError::NullPointerException("Duration").exception(env, null_mut)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isBigDecimal<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || false as jboolean);
        matches!(
            value.as_ref(),
            Value::Number(Number::Decimal(_) | Number::BigDecimal(_))
        ) as jboolean
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_getBigDecimal<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, null_mut);
        match value.as_ref() {
            Value::Number(Number::Decimal(d)) => new_string!(env, &d.to_string(), null_mut),
            Value::Number(Number::BigDecimal(d)) => new_string!(env, &d.to_string(), null_mut),
            _ => SurrealError::NullPointerException("BigDecimal").exception(env, null_mut),
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isDouble<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || false as jboolean);
        matches!(value.as_ref(), Value::Number(n) if number_as_double(n).is_some()) as jboolean
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_getDouble<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jdouble {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || 0.0);
        match value.as_ref() {
            Value::Number(n) => match number_as_double(n) {
                Some(f) => f,
                None => SurrealError::NullPointerException("Double").exception(env, || 0.0),
            },
            _ => SurrealError::NullPointerException("Double").exception(env, || 0.0),
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isGeometry<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || false as jboolean);
        value.is_geometry() as jboolean
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_getGeometry<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || 0);
        if let Value::Geometry(_) = value.as_ref() {
            JniTypes::new_value(value)
        } else {
            SurrealError::NullPointerException("Geometry").exception(env, || 0)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isNone<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || false as jboolean);
        value.is_none() as jboolean
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isNull<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || false as jboolean);
        value.is_null() as jboolean
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isString<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || false as jboolean);
        value.is_string() as jboolean
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_getString<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, null_mut);
        if let Value::String(s) = value.as_ref() {
            new_string!(env, s.clone(), null_mut)
        } else {
            SurrealError::NullPointerException("String").exception(env, null_mut)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isRecordId<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || false as jboolean);
        matches!(value.as_ref(), Value::RecordId(_)) as jboolean
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_getRecordId<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || 0);
        if let Value::RecordId(_) = value.as_ref() {
            JniTypes::new_value(value)
        } else {
            SurrealError::NullPointerException("RecordId").exception(env, || 0)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isUuid<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || false as jboolean);
        value.is_uuid() as jboolean
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_getUuid<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, null_mut);
        if let Value::Uuid(uuid) = value.as_ref() {
            new_string!(env, uuid.to_string(), null_mut)
        } else {
            SurrealError::NullPointerException("Uuid").exception(env, null_mut)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isFile<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || false as jboolean);
        value.is_file() as jboolean
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_getFile<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || 0);
        if value.is_file() {
            JniTypes::new_value(value)
        } else {
            SurrealError::NullPointerException("File").exception(env, || 0)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isSet<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || false as jboolean);
        matches!(value.as_ref(), Value::Set(_)) as jboolean
    })
}

/// Returns the elements of a set, in the set's order, as an array value.
#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_getSet<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || 0);
        if let Value::Set(set) = value.as_ref() {
            let elements: Vec<Value> = set.iter().cloned().collect();
            JniTypes::new_value(Arc::new(Value::Array(Array::from(elements))))
        } else {
            SurrealError::NullPointerException("Set").exception(env, || 0)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isRange<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || false as jboolean);
        value.is_range() as jboolean
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_getRangeStart<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || 0);
        if let Value::Range(r) = value.as_ref() {
            match &r.start {
                Bound::Included(v) | Bound::Excluded(v) => JniTypes::new_value(Arc::new(v.clone())),
                Bound::Unbounded => 0,
            }
        } else {
            SurrealError::NullPointerException("Range").exception(env, || 0)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_getRangeEnd<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || 0);
        if let Value::Range(r) = value.as_ref() {
            match &r.end {
                Bound::Included(v) | Bound::Excluded(v) => JniTypes::new_value(Arc::new(v.clone())),
                Bound::Unbounded => 0,
            }
        } else {
            SurrealError::NullPointerException("Range").exception(env, || 0)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isRangeStartInclusive<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || false as jboolean);
        if let Value::Range(r) = value.as_ref() {
            matches!(r.start, Bound::Included(_)) as jboolean
        } else {
            SurrealError::NullPointerException("Range").exception(env, || false as jboolean)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isRangeEndInclusive<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || false as jboolean);
        if let Value::Range(r) = value.as_ref() {
            matches!(r.end, Bound::Included(_)) as jboolean
        } else {
            SurrealError::NullPointerException("Range").exception(env, || false as jboolean)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_isTable<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || false as jboolean);
        value.is_table() as jboolean
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_getTable<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, null_mut);
        if let Value::Table(t) = value.as_ref() {
            new_string!(env, t.as_str(), null_mut)
        } else {
            SurrealError::NullPointerException("Table").exception(env, null_mut)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_toString<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, null_mut);
        let s = value.to_sql();
        new_string!(env, s, null_mut)
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_hashCode<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jint {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || 0);
        let mut hasher = DefaultHasher::new();
        value.hash(&mut hasher);
        let hash64 = hasher.finish();
        (hash64 & 0xFFFFFFFF) as jint
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Value_equals<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr1: jlong,
    ptr2: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let v1 = get_value_instance!(env, ptr1, || false as jboolean);
        let v2 = get_value_instance!(env, ptr2, || false as jboolean);
        v1.eq(&v2) as jboolean
    })
}
