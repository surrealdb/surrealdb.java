use crate::error::SurrealError;
#[cfg(debug_assertions)]
use dashmap::DashMap;
use jni::objects::{JObjectArray, JString};
use jni::sys::jlong;
use jni::JNIEnv;
use once_cell::sync::Lazy;
use parking_lot::Mutex;
use std::collections::btree_map::IntoIter as BIntoIter;
use std::ops::Deref;
use std::sync::Arc;
use std::vec::IntoIter;
use surrealdb::types::Value;
use surrealdb::{Connection, IndexedResults, Surreal};
use tokio::runtime::Runtime;

mod array;
mod entry;
mod entryiterator;
mod entrymut;
mod error;
mod geometry;
mod id;
mod macros;
mod object;
mod recordid;
mod response;
mod surreal;
mod syncentryiterator;
mod syncvalueiterator;
mod value;
mod valueiterator;
mod valuemut;

static TOKIO_RUNTIME: Lazy<Runtime> =
    Lazy::new(|| Runtime::new().expect("Cannot start Tokio runtime"));

#[cfg(debug_assertions)]
type Allocations = DashMap<jlong, JniTypes>;

#[derive(PartialEq)]
enum JniTypes {
    Surreal,
    Value,
    ValueMut,
    ArrayIter,
    SyncArrayIter,
    KeyValueEntry,
    KeyValueMutEntry,
    ObjectIter,
    SyncObjectIter,
    Response,
}

impl JniTypes {
    fn new_surreal<C: Connection>(s: Surreal<C>) -> jlong {
        create_instance(s, Self::Surreal)
    }

    fn new_value(v: Arc<Value>) -> jlong {
        create_instance(v, Self::Value)
    }

    fn new_value_mut(v: Value) -> jlong {
        create_instance(v, Self::ValueMut)
    }

    fn new_array_iter(i: IntoIter<Value>) -> jlong {
        create_instance(i, Self::ArrayIter)
    }

    fn new_sync_array_iter(i: Arc<Mutex<IntoIter<Value>>>) -> jlong {
        create_instance(i, Self::SyncArrayIter)
    }

    fn new_key_value(key: String, value: Arc<Value>) -> jlong {
        create_instance((key, value), Self::KeyValueEntry)
    }

    fn new_key_value_mut(key: String, value: Value) -> jlong {
        create_instance((key, value), Self::KeyValueMutEntry)
    }

    fn new_sync_object_iter(i: Arc<Mutex<BIntoIter<String, Value>>>) -> jlong {
        create_instance(i, Self::SyncObjectIter)
    }

    fn new_object_iter(i: BIntoIter<String, Value>) -> jlong {
        create_instance(i, Self::ObjectIter)
    }

    fn new_response(res: Arc<Mutex<IndexedResults>>) -> jlong {
        create_instance(res, Self::Response)
    }

    fn as_str(&self) -> &'static str {
        match self {
            JniTypes::Surreal => "Surreal",
            JniTypes::Value => "Value",
            JniTypes::ValueMut => "MutableValue",
            JniTypes::ArrayIter => "ArrayIterator",
            JniTypes::SyncArrayIter => "SynchronizedArrayIterator",
            JniTypes::KeyValueEntry => "ObjectEntry",
            JniTypes::KeyValueMutEntry => "MutableObjectEntry",
            JniTypes::ObjectIter => "ObjectIterator",
            JniTypes::SyncObjectIter => "SynchronizedObjectIterator",
            JniTypes::Response => "Response",
        }
    }
}

impl Deref for JniTypes {
    type Target = str;

    fn deref(&self) -> &Self::Target {
        self.as_str()
    }
}

#[cfg(debug_assertions)]
static ALLOCATOR: Lazy<Allocations> = Lazy::new(Allocations::default);

fn create_instance<T>(instance: T, _typ: JniTypes) -> jlong {
    // Enclose the instance in an arc
    let instance = Box::new(instance);
    // Convert it into a ptr
    let ptr = Box::into_raw(instance) as jlong;
    // Keep trace of the type
    #[cfg(debug_assertions)]
    ALLOCATOR.insert(ptr, _typ);
    ptr
}

#[cfg(debug_assertions)]
fn check_allocation(ptr: jlong, t: JniTypes) -> Result<(), SurrealError> {
    if let Some(e) = ALLOCATOR.get(&ptr) {
        let ty = e.value();
        if !t.eq(ty) {
            return Err(SurrealError::SurrealDBJni(format!(
                "Wrong type. Expected {} but got {}",
                t.as_str(),
                ty.as_str()
            )));
        }
    } else {
        return Err(SurrealError::SurrealDBJni(format!(
            "Invalid pointer for {}",
            t.as_str()
        )));
    }
    Ok(())
}

fn get_instance<T>(ptr: jlong, t: JniTypes) -> Result<&'static T, SurrealError> {
    if ptr == 0 {
        return Err(SurrealError::NullPointerException(t.as_str()));
    }
    #[cfg(debug_assertions)]
    check_allocation(ptr, t)?;

    // Convert jlong
    let instance = unsafe { &*(ptr as *const T) };
    //
    Ok(instance)
}

fn get_instance_mut<T>(ptr: jlong, t: JniTypes) -> Result<&'static mut T, SurrealError> {
    if ptr == 0 {
        return Err(SurrealError::NullPointerException(t.as_str()));
    }
    #[cfg(debug_assertions)]
    check_allocation(ptr, t)?;

    // Convert jlong
    let instance = unsafe { &mut *(ptr as *mut T) };
    Ok(instance)
}

fn take_instance<T>(ptr: jlong, t: JniTypes) -> Result<T, SurrealError> {
    if ptr == 0 {
        return Err(SurrealError::NullPointerException(t.as_str()));
    }
    #[cfg(debug_assertions)]
    check_allocation(ptr, t)?;

    // Convert jlong to a Box<T>, effectively taking ownership of the instance
    let instance = unsafe { Box::from_raw(ptr as *mut T) };
    Ok(*instance)
}

fn release_instance<T>(ptr: jlong) {
    if ptr != 0 {
        #[cfg(debug_assertions)]
        ALLOCATOR.remove(&ptr);
        // Convert jlong back to Arc<T> and let it go out of scope to free memory
        unsafe {
            let _ = Box::from_raw(ptr as *mut T);
        };
    }
}

// Function to read a jobjectArray of Strings into a Vec<String>
fn read_string_array(env: &mut JNIEnv, array: JObjectArray) -> Result<Vec<String>, SurrealError> {
    // Get the array length
    let len = env.get_array_length(&array)?;
    let mut res = Vec::with_capacity(len as usize);
    for i in 0..len {
        // Get the element at index i (as JObject)
        let elem = env.get_object_array_element(&array, i)?;
        // Cast JObject to JString and convert to Rust String
        let s = JString::from(elem);
        let s = env.get_string(&s)?;
        let s = String::from(s);
        res.push(s);
    }
    Ok(res)
}
