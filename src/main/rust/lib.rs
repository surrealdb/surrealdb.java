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
use surrealdb::{IndexedResults, Surreal};

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

/// Stack size SurrealDB recommends for threads that run it embedded: its query
/// executor recurses deeply, and a debug build needs more than 1 MiB.
const STACK_SIZE: usize = 10 * 1024 * 1024;

static TOKIO_RUNTIME: Lazy<Runtime> = Lazy::new(|| {
    tokio::runtime::Builder::new_multi_thread()
        .enable_all()
        .thread_stack_size(STACK_SIZE)
        .build()
        .expect("Cannot start Tokio runtime")
});

/// Runs `future` to completion on a Tokio worker thread, which has a
/// [`STACK_SIZE`] stack, and blocks the calling thread until it finishes.
///
/// The calling Java thread may have as little as 1 MiB of stack (the Linux
/// default), which SurrealDB can overflow, so the work does not run on it.
///
/// The `&'static` session and transaction references returned by
/// `get_instance` are only valid while Java holds the object. Handing them to
/// a spawned task is sound only because this function does not return until
/// the task has completed. Do not add any path that returns before the
/// `JoinHandle` resolves (no timeout, `select` or early return).
///
/// A panic in the future is re-raised on the calling thread.
fn block_on<F>(future: F) -> F::Output
where
    F: std::future::Future + Send + 'static,
    F::Output: Send + 'static,
{
    match TOKIO_RUNTIME.block_on(TOKIO_RUNTIME.spawn(future)) {
        Ok(output) => output,
        Err(err) => match err.try_into_panic() {
            Ok(panic) => std::panic::resume_unwind(panic),
            Err(err) => unreachable!("SurrealDB task was cancelled: {err}"),
        },
    }
}

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

/// The concrete Rust type boxed behind each [`JniTypes`] tag.
///
/// Each alias is the single definition of that type: the `JniTypes::new_*` and
/// `JniTypes::release_*` helpers below and the `get_*_instance!` / `take_*`
/// macros all go through it, so a creator, reader or deleter that drifts from
/// the others fails to compile instead of freeing the wrong type.
pub(crate) type SurrealHandle = Surreal<Any>;
pub(crate) type TransactionHandle = Transaction<Any>;
pub(crate) type ValueHandle = Arc<Value>;
pub(crate) type ValueMutHandle = Value;
pub(crate) type ArrayIterHandle = IntoIter<Value>;
pub(crate) type SyncArrayIterHandle = Arc<Mutex<IntoIter<Value>>>;
pub(crate) type KeyValueHandle = (String, Arc<Value>);
pub(crate) type KeyValueMutHandle = (String, Value);
pub(crate) type ObjectIterHandle = BIntoIter<String, Value>;
pub(crate) type SyncObjectIterHandle = Arc<Mutex<BIntoIter<String, Value>>>;
pub(crate) type ResponseHandle = Arc<Mutex<IndexedResults>>;

impl JniTypes {
    fn new_surreal(s: SurrealHandle) -> jlong {
        create_instance(s, Self::Surreal)
    }

    fn release_surreal(ptr: jlong) {
        release_instance::<SurrealHandle>(ptr, Self::Surreal)
    }

    fn new_transaction(t: TransactionHandle) -> jlong {
        create_instance(t, Self::Transaction)
    }

    fn release_transaction(ptr: jlong) {
        release_instance::<TransactionHandle>(ptr, Self::Transaction)
    }

    fn new_value(v: ValueHandle) -> jlong {
        create_instance(v, Self::Value)
    }

    fn release_value(ptr: jlong) {
        release_instance::<ValueHandle>(ptr, Self::Value)
    }

    fn new_value_mut(v: ValueMutHandle) -> jlong {
        create_instance(v, Self::ValueMut)
    }

    fn release_value_mut(ptr: jlong) {
        release_instance::<ValueMutHandle>(ptr, Self::ValueMut)
    }

    fn new_array_iter(i: ArrayIterHandle) -> jlong {
        create_instance(i, Self::ArrayIter)
    }

    fn release_array_iter(ptr: jlong) {
        release_instance::<ArrayIterHandle>(ptr, Self::ArrayIter)
    }

    fn new_sync_array_iter(i: SyncArrayIterHandle) -> jlong {
        create_instance(i, Self::SyncArrayIter)
    }

    fn release_sync_array_iter(ptr: jlong) {
        release_instance::<SyncArrayIterHandle>(ptr, Self::SyncArrayIter)
    }

    fn new_key_value(key: String, value: Arc<Value>) -> jlong {
        let entry: KeyValueHandle = (key, value);
        create_instance(entry, Self::KeyValueEntry)
    }

    fn release_key_value(ptr: jlong) {
        release_instance::<KeyValueHandle>(ptr, Self::KeyValueEntry)
    }

    fn new_key_value_mut(key: String, value: Value) -> jlong {
        let entry: KeyValueMutHandle = (key, value);
        create_instance(entry, Self::KeyValueMutEntry)
    }

    fn release_key_value_mut(ptr: jlong) {
        release_instance::<KeyValueMutHandle>(ptr, Self::KeyValueMutEntry)
    }

    fn new_sync_object_iter(i: SyncObjectIterHandle) -> jlong {
        create_instance(i, Self::SyncObjectIter)
    }

    fn release_sync_object_iter(ptr: jlong) {
        release_instance::<SyncObjectIterHandle>(ptr, Self::SyncObjectIter)
    }

    fn new_object_iter(i: ObjectIterHandle) -> jlong {
        create_instance(i, Self::ObjectIter)
    }

    fn release_object_iter(ptr: jlong) {
        release_instance::<ObjectIterHandle>(ptr, Self::ObjectIter)
    }

    fn new_response(res: ResponseHandle) -> jlong {
        create_instance(res, Self::Response)
    }

    fn release_response(ptr: jlong) {
        release_instance::<ResponseHandle>(ptr, Self::Response)
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
    let s = get_instance::<SurrealHandle>(ptr, JniTypes::Surreal)?;
    let cloned = (*s).clone();
    Ok(JniTypes::new_surreal(cloned))
}

/// Frees an instance created by [`create_instance`]. `T` must be the type that
/// was boxed, which is why callers go through the typed `JniTypes::release_*`
/// helpers rather than naming `T` themselves. In debug builds the tag is
/// checked against the allocation first; on a mismatch (or an unknown or
/// already-freed pointer) nothing is freed and the problem is logged, never
/// raised across the JNI boundary.
fn release_instance<T>(ptr: jlong, _t: JniTypes) {
    if ptr == 0 {
        return;
    }
    #[cfg(debug_assertions)]
    {
        if let Err(SurrealError::SurrealDBJni(msg)) = check_allocation(ptr, _t) {
            eprintln!("surrealdb-java: refusing to release instance: {msg}");
            return;
        }
        ALLOCATOR.remove(&ptr);
    }
    // Convert jlong back to Box<T> and let it go out of scope to free memory
    unsafe {
        let _ = Box::from_raw(ptr as *mut T);
    };
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
