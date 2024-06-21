use std::sync::Arc;

use jni::sys::jlong;
use once_cell::sync::Lazy;
use tokio::runtime::Runtime;

use crate::error::DriverError;

mod error;
mod macros;
mod surreal;


static TOKIO_RUNTIME: Lazy<Runtime> =
    Lazy::new(|| Runtime::new().expect("Cannot start Tokio runtime"));

fn create_arc_instance<T>(instance: T) -> jlong {
    // Enclose the instance in an arc
    let instance = Box::new(Arc::new(instance));
    // Convert it into a ptr
    Box::into_raw(instance) as jlong
}

fn get_arc_instance<T>(id: jlong, name: &'static str) -> Result<Arc<T>, DriverError> {
    if id == 0 {
        return Err(DriverError::InstanceNotFound(name));
    }
    // Convert jlong
    let instance = unsafe { &*(id as *const Arc<T>) };
    Ok(Arc::clone(instance))
}

fn release_arc_instance<T>(id: jlong) {
    if id != 0 {
        // Convert jlong back to Arc<T> and let it go out of scope to free memory
        unsafe { let _ = Box::from_raw(id as *mut Arc<T>); };
    }
}