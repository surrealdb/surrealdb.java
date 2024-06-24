use jni::sys::jlong;
use once_cell::sync::Lazy;
use tokio::runtime::Runtime;

use crate::error::SurrealError;

mod array;
mod entry;
mod entryiterator;
mod error;
mod geometry;
mod id;
mod macros;
mod object;
mod response;
mod surreal;
mod syncentryiterator;
mod syncvalueiterator;
mod thing;
mod value;
mod valueiterator;

static TOKIO_RUNTIME: Lazy<Runtime> =
    Lazy::new(|| Runtime::new().expect("Cannot start Tokio runtime"));

fn create_instance<T>(instance: T) -> jlong {
    // Enclose the instance in an arc
    let instance = Box::new(instance);
    // Convert it into a ptr
    Box::into_raw(instance) as jlong
}

fn get_instance<T>(ptr: jlong, name: &'static str) -> Result<&T, SurrealError> {
    if ptr == 0 {
        return Err(SurrealError::NullPointerException(name));
    }
    // Convert jlong
    let instance = unsafe { &*(ptr as *const T) };
    Ok(instance)
}

fn get_instance_mut<T>(ptr: jlong, name: &'static str) -> Result<&mut T, SurrealError> {
    if ptr == 0 {
        return Err(SurrealError::NullPointerException(name));
    }
    // Convert jlong
    let instance = unsafe { &mut *(ptr as *mut T) };
    Ok(instance)
}

fn release_instance<T>(ptr: jlong) {
    if ptr != 0 {
        // Convert jlong back to Arc<T> and let it go out of scope to free memory
        unsafe {
            let _ = Box::from_raw(ptr as *mut T);
        };
    }
}
