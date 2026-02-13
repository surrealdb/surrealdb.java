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
use surrealdb::engine::any::Any;
use surrealdb::method::Transaction;
use surrealdb::types::Value;
use surrealdb::{Connection, IndexedResults, Surreal};

/// Item type for the live query channel (Result<Notification<Value>>).
pub(crate) type LiveNotificationResult =
    std::result::Result<surrealdb::Notification<surrealdb::types::Value>, surrealdb::Error>;

/// Stored as handle for live streams. recv_mutex is held by nextNative during recv() so that
/// releaseNative can wait for no thread in recv() before taking and dropping the receiver.
/// join_handle, shutdown_tx and rx are in Mutex<Option<..>> so releaseNative can take/drop them via get_instance.
pub(crate) type LiveStreamChannel = (
    std::sync::Arc<parking_lot::Mutex<()>>, // held during recv()
    parking_lot::Mutex<Option<std::thread::JoinHandle<()>>>,
    parking_lot::Mutex<Option<async_channel::Sender<()>>>,
    parking_lot::Mutex<Option<async_channel::Receiver<LiveNotificationResult>>>,
);
use tokio::runtime::Runtime;

mod array;
mod entry;
mod entryiterator;
mod entrymut;
mod error;
mod fileref;
mod geometry;
mod id;
mod live;
mod macros;
mod object;
mod recordid;
mod response;
mod surreal;
mod transaction;
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
    Transaction,
    Value,
    ValueMut,
    ArrayIter,
    SyncArrayIter,
    KeyValueEntry,
    KeyValueMutEntry,
    ObjectIter,
    SyncObjectIter,
    Response,
    LiveStream,
}

impl JniTypes {
    fn new_surreal<C: Connection>(s: Surreal<C>) -> jlong {
        create_instance(s, Self::Surreal)
    }

    fn new_transaction<C: Connection>(t: Transaction<C>) -> jlong {
        create_instance(t, Self::Transaction)
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

    fn new_live_stream(chan: LiveStreamChannel) -> jlong {
        create_instance(chan, Self::LiveStream)
    }

    fn as_str(&self) -> &'static str {
        match self {
            JniTypes::Surreal => "Surreal",
            JniTypes::Transaction => "Transaction",
            JniTypes::Value => "Value",
            JniTypes::ValueMut => "MutableValue",
            JniTypes::ArrayIter => "ArrayIterator",
            JniTypes::SyncArrayIter => "SynchronizedArrayIterator",
            JniTypes::KeyValueEntry => "ObjectEntry",
            JniTypes::KeyValueMutEntry => "MutableObjectEntry",
            JniTypes::ObjectIter => "ObjectIterator",
            JniTypes::SyncObjectIter => "SynchronizedObjectIterator",
            JniTypes::Response => "Response",
            JniTypes::LiveStream => "LiveStream",
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

pub(crate) fn take_instance<T>(ptr: jlong, t: JniTypes) -> Result<T, SurrealError> {
    if ptr == 0 {
        return Err(SurrealError::NullPointerException(t.as_str()));
    }
    #[cfg(debug_assertions)]
    {
        check_allocation(ptr, t)?;
        ALLOCATOR.remove(&ptr);
    }

    // Convert jlong to a Box<T>, effectively taking ownership of the instance
    let instance = unsafe { Box::from_raw(ptr as *mut T) };
    Ok(*instance)
}

/// Clones the Surreal instance at the given pointer and returns a new pointer to the clone.
/// Used for multi-session support: each clone is a separate session sharing the same connection.
pub(crate) fn clone_surreal_instance(ptr: jlong) -> Result<jlong, SurrealError> {
    let s = get_instance::<Surreal<Any>>(ptr, JniTypes::Surreal)?;
    let cloned = (*s).clone();
    Ok(JniTypes::new_surreal(cloned))
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
