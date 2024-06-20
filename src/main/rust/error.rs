use jni::errors::{Error, Exception, ToException};
use jni::JNIEnv;
use jni::sys::jobject;

pub(super) enum DriverError {
    Error(Error),
    InstanceNotFound(i32),
    SurrealDB(surrealdb::Error),
}

const EXCEPTION: &str = "java/lang/exception";
const ILLEGAL_ARGUMENT_EXCEPTION: &str = "java/lang/IllegalArgumentException";
const SURREALDB_EXCEPTION: &str = "com/surrealdb/SurrealDBException";

impl ToException for DriverError {
    fn to_exception(&self) -> Exception {
        match self {
            Self::Error(e) => Exception { class: EXCEPTION.to_string(), msg: format!("{e}") },
            Self::InstanceNotFound(id) => Exception { class: ILLEGAL_ARGUMENT_EXCEPTION.to_string(), msg: format!("Surreal instance not found ({id})") },
            Self::SurrealDB(e) => Exception { class: SURREALDB_EXCEPTION.to_string(), msg: format!("{e}") }
        }
    }
}

impl DriverError {
    pub(super) fn exception(self, env: &mut JNIEnv) -> jobject {
        if let Ok(b) = env.exception_check() {
            // If there is already an exception thrown we don't add one
            if !b {
                let _ = env.throw(self.to_exception());
            }
        }
        // A method returning an exception should return null
        std::ptr::null_mut()
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