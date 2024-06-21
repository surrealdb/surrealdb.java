use jni::errors::{Error, Exception, ToException};
use jni::JNIEnv;

pub(super) enum DriverError {
    Error(Error),
    InstanceNotFound(&'static str),
    SurrealDB(surrealdb::Error),
}

const EXCEPTION: &str = "java/lang/exception";
const NULL_POINTER_EXCEPTION: &str = "java/lang/NullPointerException";
const SURREALDB_EXCEPTION: &str = "com/surrealdb/SurrealDBException";

impl ToException for DriverError {
    fn to_exception(&self) -> Exception {
        match self {
            Self::Error(e) => Exception { class: EXCEPTION.to_string(), msg: format!("{e}") },
            Self::InstanceNotFound(s) => Exception { class: NULL_POINTER_EXCEPTION.to_string(), msg: format!("{s} instance not found") },
            Self::SurrealDB(e) => Exception { class: SURREALDB_EXCEPTION.to_string(), msg: format!("{e}") }
        }
    }
}

impl DriverError {
    pub(super) fn exception<T, F: FnOnce() -> T>(self, env: &mut JNIEnv, output: F) -> T {
        if let Ok(b) = env.exception_check() {
            // If there is already an exception thrown we don't add one
            if !b {
                let _ = env.throw(self.to_exception());
            }
        }
        output()
    }
}

impl From<Error> for DriverError {
    fn from(e: Error) -> Self {
        DriverError::Error(e)
    }
}

impl From<surrealdb::Error> for DriverError {
    fn from(e: surrealdb::Error) -> Self {
        DriverError::SurrealDB(e)
    }
}