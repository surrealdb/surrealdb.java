use crate::error::SurrealError;
#[cfg(debug_assertions)]
use dashmap::DashMap;
use jni::objects::{JObjectArray, JString};
use jni::sys::jlong;
use jni::{Env, EnvOutcome, Outcome};
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

/// Item type for the live query notification channel.
pub(crate) type LiveNotificationResult =
    std::result::Result<surrealdb::Notification<surrealdb::types::Value>, surrealdb::Error>;

/// Native handle backing a Java `LiveStream` instance.
///
/// Created by `selectLive` (in surreal.rs) after the live query subscription
/// is confirmed, and freed by `releaseNative` (in live.rs) when the Java side
/// calls `close()`.
///
/// ## Fields (tuple elements)
///
/// 0. **`recv_mutex`** (`Arc<Mutex<()>>`) — held by `nextNative` for the
///    entire duration of the blocking `recv()` call.  `releaseNative` acquires
///    it *after* the channel has been closed so it can be sure no thread is
///    still inside `recv()` before freeing the handle.
///
/// 1. **`join_handle`** (`Mutex<Option<JoinHandle>>`) — the background thread
///    that reads from the SurrealDB live-query stream and forwards
///    notifications into the async channel.  Taken and joined by
///    `releaseNative` during shutdown.
///
/// 2. **`shutdown_tx`** (`Mutex<Option<Sender<()>>>`) — dropping this sender
///    signals the background thread (via `tokio::select!`) to exit.
///
/// 3. **`rx`** (`Mutex<Option<Receiver<LiveNotificationResult>>>`) — the
///    receiving end of the notification channel, read by `nextNative`.
///
/// ## Lock ordering
///
/// Both `nextNative` and `releaseNative` acquire `recv_mutex` **before**
/// `rx`, ensuring a consistent ordering and preventing deadlocks.
pub(crate) type LiveStreamChannel = (
    std::sync::Arc<parking_lot::Mutex<()>>,
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
mod syncentryiterator;
mod syncvalueiterator;
mod transaction;
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
fn read_string_array(
    env: &mut Env,
    array: JObjectArray<JString>,
) -> Result<Vec<String>, SurrealError> {
    // Get the array length (jni 0.22: methods moved onto the array type; len() -> usize)
    let len = array.len(env)?;
    let mut res = Vec::with_capacity(len);
    for i in 0..len {
        // Typed element access yields a JString directly; convert to a Rust String
        let s = array.get_element(env, i)?;
        res.push(s.try_to_string(env)?);
    }
    Ok(res)
}

/// Resolves an [`EnvOutcome`] produced by `EnvUnowned::with_env` for a native-method
/// body. The body always returns `Ok` (application errors are surfaced as Java
/// exceptions inside the body via [`SurrealError::exception`]), so the `Err` arm is
/// unreachable. Panics are re-raised so they abort at the `extern "system"` boundary,
/// matching pre-migration behaviour. Using `into_outcome` here (rather than
/// `EnvOutcome::resolve`) avoids a `T: Default` bound, letting the exports keep their
/// existing raw `jobject`/`jstring`/`jlong` return types and the structured-exception sink.
pub(crate) fn resolve_outcome<T>(outcome: EnvOutcome<'_, T, jni::errors::Error>) -> T {
    match outcome.into_outcome() {
        Outcome::Ok(v) => v,
        Outcome::Err(_e) => {
            unreachable!("JNI errors are surfaced as Java exceptions within the native body")
        }
        Outcome::Panic(p) => std::panic::resume_unwind(p),
    }
}
