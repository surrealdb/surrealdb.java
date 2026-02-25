use std::error::Error as StdError;

use jni::errors::Error;
use jni::objects::{JObject, JThrowable, JValue};
use jni::JNIEnv;
use surrealdb::types::SurrealValue;

pub(super) enum SurrealError {
    Exception(Error),
    NullPointerException(&'static str),
    NoSuchElementException,
    SurrealDB(surrealdb::Error),
    SurrealDBJni(String),
}

const EXCEPTION: &str = "java/lang/exception";
const NULL_POINTER_EXCEPTION: &str = "java/lang/NullPointerException";
const NO_SUCH_ELEMENT_EXCEPTION: &str = "java/util/NoSuchElementException";
const SURREAL_EXCEPTION: &str = "com/surrealdb/SurrealException";

const SERVER_EXCEPTION: &str = "com/surrealdb/ServerException";

/// Maps an error kind string to the corresponding Java exception class.
/// Unknown kinds map to ServerException (NOT InternalException) for forward compatibility.
fn kind_to_java_class(kind: &str) -> &'static str {
    match kind {
        "Validation" => "com/surrealdb/ValidationException",
        "Configuration" => "com/surrealdb/ConfigurationException",
        "Thrown" => "com/surrealdb/ThrownException",
        "Query" => "com/surrealdb/QueryException",
        "Serialization" => "com/surrealdb/SerializationException",
        "NotAllowed" => "com/surrealdb/NotAllowedException",
        "NotFound" => "com/surrealdb/NotFoundException",
        "AlreadyExists" => "com/surrealdb/AlreadyExistsException",
        "Connection" => SERVER_EXCEPTION,
        "Internal" => "com/surrealdb/InternalException",
        // Forward compat: unknown kinds get base ServerException
        _ => SERVER_EXCEPTION,
    }
}

/// Serializes details to a JSON string.
///
/// In 3.0.1, details are converted via SurrealValue::into_value and then
/// serialized to JSON. We also unwrap double-wrapped details produced by
/// SurrealDB v3.0.0 (when the outer `kind` matches the error kind, extract
/// the inner "details") for backward compatibility.
fn details_to_json(error: &surrealdb::Error) -> Option<String> {
    let value = SurrealValue::into_value(error.details().clone());
    let json = serde_json::to_string(&value).ok()?;

    // Unwrap double-encoded details: if the serialized JSON object has
    // a "kind" that matches the error kind, extract the inner "details".
    let kind_str = error.kind_str();
    if let Ok(serde_json::Value::Object(map)) = serde_json::from_str::<serde_json::Value>(&json) {
        if map.get("kind").and_then(|v| v.as_str()) == Some(kind_str) {
            if let Some(inner) = map.get("details") {
                return serde_json::to_string(inner).ok();
            }
        }
    }

    Some(json)
}

/// Recursively builds a Java ServerException (or subclass) from a surrealdb::Error,
/// constructing the cause chain bottom-up.
///
/// Subclasses use a 3-arg constructor: (String message, String detailsJson, ServerException cause)
/// Base ServerException uses a 4-arg constructor: (String kind, String message, String detailsJson, ServerException cause)
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

    let kind_str = error.kind_str();
    let class_name = kind_to_java_class(kind_str);
    let is_base_class = class_name == SERVER_EXCEPTION;

    let class = match env.find_class(class_name) {
        Ok(c) => c,
        Err(_) => return None,
    };

    let message = match env.new_string(error.message()) {
        Ok(s) => s,
        Err(_) => return None,
    };

    let details_json = details_to_json(error);
    let details_jstr = match &details_json {
        Some(json) => match env.new_string(json) {
            Ok(s) => JObject::from(s),
            Err(_) => JObject::null(),
        },
        None => JObject::null(),
    };

    let message_obj = JObject::from(message);

    if is_base_class {
        // ServerException(String kind, String message, String detailsJson, ServerException cause)
        let kind_str = match env.new_string(kind_str) {
            Ok(s) => s,
            Err(_) => return None,
        };
        let kind_obj = JObject::from(kind_str);
        let sig = "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Lcom/surrealdb/ServerException;)V";
        let args = [
            JValue::Object(&kind_obj),
            JValue::Object(&message_obj),
            JValue::Object(&details_jstr),
            JValue::Object(&java_cause),
        ];
        match env.new_object(class, sig, &args) {
            Ok(obj) => Some(obj),
            Err(_) => None,
        }
    } else {
        // Subclass(String message, String detailsJson, ServerException cause)
        let sig = "(Ljava/lang/String;Ljava/lang/String;Lcom/surrealdb/ServerException;)V";
        let args = [
            JValue::Object(&message_obj),
            JValue::Object(&details_jstr),
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
