use jni::errors::Error;
use jni::objects::{JObject, JThrowable, JValue};
use jni::JNIEnv;
use surrealdb::types::ErrorKind;

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

/// Maps an ErrorKind to the corresponding Java exception class.
/// Unknown kinds map to ServerException (NOT InternalException) for forward compatibility.
fn kind_to_java_class(kind: &ErrorKind) -> &'static str {
    match kind {
        ErrorKind::Validation => "com/surrealdb/ValidationException",
        ErrorKind::Configuration => "com/surrealdb/ConfigurationException",
        ErrorKind::Thrown => "com/surrealdb/ThrownException",
        ErrorKind::Query => "com/surrealdb/QueryException",
        ErrorKind::Serialization => "com/surrealdb/SerializationException",
        ErrorKind::NotAllowed => "com/surrealdb/NotAllowedException",
        ErrorKind::NotFound => "com/surrealdb/NotFoundException",
        ErrorKind::AlreadyExists => "com/surrealdb/AlreadyExistsException",
        ErrorKind::Connection => SERVER_EXCEPTION,
        ErrorKind::Internal => "com/surrealdb/InternalException",
        // Forward compat: unknown kinds get base ServerException
        _ => SERVER_EXCEPTION,
    }
}

/// Maps an ErrorKind to its string representation for the Java side.
/// Used when constructing base ServerException (Connection / unknown kinds).
fn kind_to_string(kind: &ErrorKind) -> &'static str {
    match kind {
        ErrorKind::Validation => "Validation",
        ErrorKind::Configuration => "Configuration",
        ErrorKind::Thrown => "Thrown",
        ErrorKind::Query => "Query",
        ErrorKind::Serialization => "Serialization",
        ErrorKind::NotAllowed => "NotAllowed",
        ErrorKind::NotFound => "NotFound",
        ErrorKind::AlreadyExists => "AlreadyExists",
        ErrorKind::Connection => "Connection",
        ErrorKind::Internal => "Internal",
        _ => "Internal",
    }
}

/// Serializes details Value to a JSON string, or returns None.
fn details_to_json(error: &surrealdb::Error) -> Option<String> {
    error
        .details()
        .and_then(|v| serde_json::to_string(v).ok())
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
    // Recursively build the cause first
    let java_cause: JObject = if let Some(cause) = error.cause() {
        build_server_exception(env, cause).unwrap_or_else(|| JObject::null())
    } else {
        JObject::null()
    };

    let class_name = kind_to_java_class(error.kind());
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
        let kind_str = match env.new_string(kind_to_string(error.kind())) {
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
