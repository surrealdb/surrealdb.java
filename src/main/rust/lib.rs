use jni::sys::jlong;
use once_cell::sync::Lazy;
use tokio::runtime::Runtime;

use crate::error::SurrealError;

mod error;
mod macros;
mod surreal;
mod response;
mod value;
mod object;
mod array;


static TOKIO_RUNTIME: Lazy<Runtime> =
    Lazy::new(|| Runtime::new().expect("Cannot start Tokio runtime"));

fn create_instance<T>(instance: T) -> jlong {
    // Enclose the instance in an arc
    let instance = Box::new(instance);
    // Convert it into a ptr
    Box::into_raw(instance) as jlong
}

fn get_instance<T>(id: jlong, name: &'static str) -> Result<&T, SurrealError> {
    if id == 0 {
        return Err(SurrealError::NullPointerException(name));
    }
    // Convert jlong
    let instance = unsafe { &*(id as *const T) };
    Ok(instance)
}

fn release_instance<T>(id: jlong) {
    if id != 0 {
        // Convert jlong back to Arc<T> and let it go out of scope to free memory
        unsafe { let _ = Box::from_raw(id as *mut T); };
    }
}