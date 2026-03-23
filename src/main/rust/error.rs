use std::error::Error as StdError;

use jni::errors::Error;
use jni::objects::{JObject, JThrowable, JValue};
use jni::JNIEnv;
use surrealdb::types::{ErrorDetails, Number, SurrealValue, Value};

pub(super) enum SurrealError {
    Exception(Error),
    NullPointerException(&'static str),
    NoSuchElementException,
    SurrealDB(surrealdb::Error),
    SurrealDBJni(String),
}

const EXCEPTION: &str = "java/lang/Exception";
const NULL_POINTER_EXCEPTION: &str = "java/lang/NullPointerException";
const NO_SUCH_ELEMENT_EXCEPTION: &str = "java/util/NoSuchElementException";
const SURREAL_EXCEPTION: &str = "com/surrealdb/SurrealException";

const SERVER_EXCEPTION: &str = "com/surrealdb/ServerException";

/// Converts error details (SurrealValue) into a Value we can walk.
/// Unwraps double-wrapped details from SurrealDB v3.0.0 when outer "kind"
/// matches the error kind and "details" is present.
fn details_value(error: &surrealdb::Error) -> Value {
    let value = SurrealValue::into_value(error.details().clone());
    let kind_str = error.kind_str();
    if let Value::Object(ref map) = value {
        let kind_matches = map
            .get("kind")
            .map(|v| matches!(v, Value::String(s) if s.to_string() == kind_str))
            .unwrap_or(false);
        if kind_matches {
            if let Some(inner) = map.get("details") {
                return inner.clone();
            }
        }
    }
    value
}

/// Builds a Java object (Map, String, Number, or null) from a SurrealDB Value via JNI.
/// Used for error details only; supports objects, strings, numbers, null, and arrays.
fn value_to_jobject<'a>(env: &mut JNIEnv<'a>, value: &Value) -> Option<JObject<'a>> {
    match value {
        Value::None | Value::Null => Some(JObject::null()),
        Value::String(s) => env.new_string(s.to_string()).ok().map(JObject::from),
        Value::Number(n) => number_to_jobject(env, n),
        Value::Bool(b) => {
            let class = env.find_class("java/lang/Boolean").ok()?;
            let z = if *b { 1u8 } else { 0u8 };
            env.call_static_method(
                class,
                "valueOf",
                "(Z)Ljava/lang/Boolean;",
                &[JValue::Bool(z)],
            )
            .ok()
            .and_then(|v| v.l().ok())
            .map(JObject::from)
        }
        Value::Object(map) => {
            let class = env.find_class("java/util/LinkedHashMap").ok()?;
            let map_obj = env.new_object(class, "()V", &[]).ok()?;
            for (k, v) in map.iter() {
                let key_obj = env.new_string(k).ok().map(JObject::from)?;
                let val_obj = value_to_jobject(env, v).unwrap_or(JObject::null());
                let _ = env.call_method(
                    &map_obj,
                    "put",
                    "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
                    &[JValue::Object(&key_obj), JValue::Object(&val_obj)],
                );
            }
            Some(map_obj)
        }
        Value::Array(arr) => {
            let class = env.find_class("java/util/ArrayList").ok()?;
            let list_obj = env.new_object(class, "()V", &[]).ok()?;
            for v in arr.iter() {
                let elem = value_to_jobject(env, v).unwrap_or(JObject::null());
                let _ = env.call_method(
                    &list_obj,
                    "add",
                    "(Ljava/lang/Object;)Z",
                    &[JValue::Object(&elem)],
                );
            }
            Some(list_obj)
        }
        _ => Some(JObject::null()),
    }
}

fn number_to_jobject<'a>(env: &mut JNIEnv<'a>, n: &Number) -> Option<JObject<'a>> {
    match n {
        Number::Int(i) => {
            let class = env.find_class("java/lang/Long").ok()?;
            env.call_static_method(class, "valueOf", "(J)Ljava/lang/Long;", &[JValue::Long(*i)])
                .ok()
                .and_then(|v| v.l().ok())
                .map(JObject::from)
        }
        Number::Float(f) => {
            let class = env.find_class("java/lang/Double").ok()?;
            env.call_static_method(
                class,
                "valueOf",
                "(D)Ljava/lang/Double;",
                &[JValue::Double(*f)],
            )
            .ok()
            .and_then(|v| v.l().ok())
            .map(JObject::from)
        }
        Number::Decimal(d) => {
            let class = env.find_class("java/lang/Double").ok()?;
            let f: f64 = d.to_string().parse().unwrap_or(0.0);
            env.call_static_method(
                class,
                "valueOf",
                "(D)Ljava/lang/Double;",
                &[JValue::Double(f)],
            )
            .ok()
            .and_then(|v| v.l().ok())
            .map(JObject::from)
        }
    }
}

/// Recursively builds a Java ServerException (or subclass) from a surrealdb::Error,
/// constructing the cause chain bottom-up.
///
/// Matches on the Rust SDK's ErrorDetails enum to choose the Java exception class and
/// ErrorKind enum. Base ServerException uses (ErrorKind, String rawKindIfUnknown, message, details, cause);
/// subclasses use (String message, Object details, ServerException cause).
fn build_server_exception<'a>(
    env: &mut JNIEnv<'a>,
    error: &surrealdb::Error,
) -> Option<JObject<'a>> {
    // Recursively build the cause first (std::error::Error::source; cause chain when present)
    let java_cause: JObject = if let Some(source) = error.source() {
        if let Some(surreal_cause) = source.downcast_ref::<surrealdb::Error>() {
            build_server_exception(env, surreal_cause).unwrap_or_else(|| JObject::null())
        } else {
            JObject::null()
        }
    } else {
        JObject::null()
    };

    // Match on the Rust SDK's ErrorDetails enum to align with the typed API.
    let (class_name, enum_name, raw_kind_for_unknown) = match error.details() {
        ErrorDetails::Validation(_) => ("com/surrealdb/ValidationException", "VALIDATION", None),
        ErrorDetails::Configuration(_) => (
            "com/surrealdb/ConfigurationException",
            "CONFIGURATION",
            None,
        ),
        ErrorDetails::Thrown => ("com/surrealdb/ThrownException", "THROWN", None),
        ErrorDetails::Query(_) => ("com/surrealdb/QueryException", "QUERY", None),
        ErrorDetails::Serialization(_) => (
            "com/surrealdb/SerializationException",
            "SERIALIZATION",
            None,
        ),
        ErrorDetails::NotAllowed(_) => ("com/surrealdb/NotAllowedException", "NOT_ALLOWED", None),
        ErrorDetails::NotFound(_) => ("com/surrealdb/NotFoundException", "NOT_FOUND", None),
        ErrorDetails::AlreadyExists(_) => (
            "com/surrealdb/AlreadyExistsException",
            "ALREADY_EXISTS",
            None,
        ),
        ErrorDetails::Connection(_) => (SERVER_EXCEPTION, "CONNECTION", None),
        ErrorDetails::Internal => ("com/surrealdb/InternalException", "INTERNAL", None),
        _ => (SERVER_EXCEPTION, "UNKNOWN", Some(error.kind_str())),
    };
    let is_base_class = class_name == SERVER_EXCEPTION;

    let class = match env.find_class(class_name) {
        Ok(c) => c,
        Err(_) => return None,
    };

    let message = match env.new_string(error.message()) {
        Ok(s) => s,
        Err(_) => return None,
    };

    let details_value = details_value(error);
    let details_obj = value_to_jobject(env, &details_value).unwrap_or(JObject::null());

    let message_obj = JObject::from(message);

    if is_base_class {
        // ServerException(ErrorKind kind, String rawKindIfUnknown, String message, Object details, ServerException cause)
        let enum_class = env.find_class("com/surrealdb/ErrorKind").ok()?;
        let enum_obj = env
            .get_static_field(&enum_class, enum_name, "Lcom/surrealdb/ErrorKind;")
            .ok()?
            .l()
            .ok()
            .map(JObject::from)?;
        let raw_kind_jstr = match raw_kind_for_unknown {
            Some(s) => env
                .new_string(s)
                .ok()
                .map(JObject::from)
                .unwrap_or(JObject::null()),
            None => JObject::null(),
        };
        let sig = "(Lcom/surrealdb/ErrorKind;Ljava/lang/String;Ljava/lang/String;Ljava/lang/Object;Lcom/surrealdb/ServerException;)V";
        let args = [
            JValue::Object(&enum_obj),
            JValue::Object(&raw_kind_jstr),
            JValue::Object(&message_obj),
            JValue::Object(&details_obj),
            JValue::Object(&java_cause),
        ];
        match env.new_object(class, sig, &args) {
            Ok(obj) => Some(obj),
            Err(_) => None,
        }
    } else {
        // Subclass(String message, Object details, ServerException cause)
        let sig = "(Ljava/lang/String;Ljava/lang/Object;Lcom/surrealdb/ServerException;)V";
        let args = [
            JValue::Object(&message_obj),
            JValue::Object(&details_obj),
            JValue::Object(&java_cause),
        ];
        match env.new_object(class, sig, &args) {
            Ok(obj) => Some(obj),
            Err(_) => None,
        }
    }
}

impl SurrealError {
    pub(super) fn exception<T, F: FnOnce() -> T>(self, env: &mut JNIEnv, output: F) -> T {
        if let Ok(b) = env.exception_check() {
            // If there is already an exception thrown we don't add one
            if !b {
                match &self {
                    Self::SurrealDB(e) => {
                        // Build structured server exception via JNI
                        if let Some(exc) = build_server_exception(env, e) {
                            let throwable: JThrowable = exc.into();
                            let _ = env.throw(throwable);
                        } else {
                            // Fallback: flat string
                            let _ = env.throw_new(SURREAL_EXCEPTION, e.to_string());
                        }
                    }
                    _ => {
                        let exc = self.into_exception();
                        let _ = env.throw_new(exc.class, exc.msg);
                    }
                }
            }
        }
        output()
    }

    fn into_exception(self) -> SimpleException {
        match self {
            Self::Exception(e) => SimpleException {
                class: EXCEPTION.to_string(),
                msg: e.to_string(),
            },
            Self::NullPointerException(t) => SimpleException {
                class: NULL_POINTER_EXCEPTION.to_string(),
                msg: format!("{t} instance not found"),
            },
            Self::NoSuchElementException => SimpleException {
                class: NO_SUCH_ELEMENT_EXCEPTION.to_string(),
                msg: "No more elements".to_string(),
            },
            Self::SurrealDB(e) => SimpleException {
                class: SURREAL_EXCEPTION.to_string(),
                msg: e.to_string(),
            },
            Self::SurrealDBJni(msg) => SimpleException {
                class: SURREAL_EXCEPTION.to_string(),
                msg: msg.to_string(),
            },
        }
    }
}

/// Simple class+message pair for non-SurrealDB exceptions.
struct SimpleException {
    class: String,
    msg: String,
}

impl From<Error> for SurrealError {
    fn from(e: Error) -> Self {
        SurrealError::Exception(e)
    }
}

impl From<surrealdb::Error> for SurrealError {
    fn from(e: surrealdb::Error) -> Self {
        SurrealError::SurrealDB(e)
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    /// Validates that JNI class name constants use correct Java casing.
    /// Java class names are case-sensitive in JNI; e.g. "java/lang/Exception"
    /// is valid but "java/lang/exception" would fail at runtime.
    #[test]
    fn jni_class_names_are_correctly_cased() {
        assert_eq!(EXCEPTION, "java/lang/Exception");
        assert_eq!(NULL_POINTER_EXCEPTION, "java/lang/NullPointerException");
        assert_eq!(
            NO_SUCH_ELEMENT_EXCEPTION,
            "java/util/NoSuchElementException"
        );
        assert_eq!(SURREAL_EXCEPTION, "com/surrealdb/SurrealException");
        assert_eq!(SERVER_EXCEPTION, "com/surrealdb/ServerException");
    }

    /// Validates that each JNI class name segment that represents a class
    /// (the last segment) starts with an uppercase letter, following Java conventions.
    #[test]
    fn jni_class_names_have_uppercase_class_segment() {
        let constants = [
            EXCEPTION,
            NULL_POINTER_EXCEPTION,
            NO_SUCH_ELEMENT_EXCEPTION,
            SURREAL_EXCEPTION,
            SERVER_EXCEPTION,
        ];
        for name in constants {
            let class_segment = name.rsplit('/').next().unwrap();
            assert!(
                class_segment.starts_with(char::is_uppercase),
                "JNI class name '{}' has a class segment '{}' that does not start with an uppercase letter",
                name,
                class_segment
            );
        }
    }
}
