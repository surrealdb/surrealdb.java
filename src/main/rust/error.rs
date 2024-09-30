use jni::errors::{Error, Exception};
use jni::JNIEnv;

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

impl SurrealError {
    pub(super) fn exception<T, F: FnOnce() -> T>(self, env: &mut JNIEnv, output: F) -> T {
        if let Ok(b) = env.exception_check() {
            // If there is already an exception thrown we don't add one
            if !b {
                let _ = env.throw(self.into_exception());
            }
        }
        output()
    }

    fn into_exception(self) -> Exception {
        match self {
            Self::Exception(e) => Exception {
                class: EXCEPTION.to_string(),
                msg: e.to_string(),
            },
            Self::NullPointerException(t) => Exception {
                class: NULL_POINTER_EXCEPTION.to_string(),
                msg: format!("{t} instance not found"),
            },
            Self::NoSuchElementException => Exception {
                class: NO_SUCH_ELEMENT_EXCEPTION.to_string(),
                msg: "No more elements".to_string(),
            },
            Self::SurrealDB(e) => Exception {
                class: SURREAL_EXCEPTION.to_string(),
                msg: e.to_string(),
            },
            Self::SurrealDBJni(msg) => Exception {
                class: SURREAL_EXCEPTION.to_string(),
                msg: msg.to_string(),
            },
        }
    }
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
