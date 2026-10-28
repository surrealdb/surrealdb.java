use std::collections::BTreeMap;
use std::path::Path;
use std::ptr::null_mut;
use std::str::FromStr;
use std::sync::Arc;

use crate::error::SurrealError;
use crate::with_env_body;
use crate::{
    build_params_map, check_query_result, check_table_binding, convert_up_type, get_long_array,
    get_rust_string, get_rust_string_array, get_surreal_ref, get_value_instance,
    get_value_mut_instance, new_jlong_array, new_string, return_unexpected_result,
    return_value_array_first, return_value_array_iter, return_value_array_iter_sync,
    take_one_result, JniTypes, TOKIO_RUNTIME,
};
use futures::StreamExt;
use jni::objects::{JClass, JLongArray, JObject, JObjectArray, JString, JValue};
use jni::sys::{jboolean, jint, jlong, jlongArray, jobject, jstring};
use jni::{jni_sig, jni_str, Env, EnvUnowned};
use parking_lot::Mutex;
use serde::Serialize;
use std::ops::Bound;
use std::result::Result as StdResult;
use surrealdb::engine::any::Any;
use surrealdb::opt::auth::{Database, Namespace, Record as AuthRecord, Root};
use surrealdb::types::{
    Array, RecordId, RecordIdKey, RecordIdKeyRange, SurrealValue, Table, ToSql, Uuid, Value,
};
use surrealdb::{IndexedResults, Result, Surreal};

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_beginTransaction<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        let surreal = get_surreal_ref!(env, ptr, || 0);
        match crate::block_on(async move { surreal.clone().begin().await }) {
            Ok(txn) => JniTypes::new_transaction(txn),
            Err(e) => SurrealError::from(e).exception(env, || 0),
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_cloneSession<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        match crate::clone_surreal_instance(ptr) {
            Ok(new_ptr) => new_ptr,
            Err(e) => e.exception(env, || 0),
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_newInstance<'local>(
    _env: EnvUnowned<'local>,
    _class: JClass<'local>,
) -> jlong {
    JniTypes::new_surreal(Surreal::<Any>::init())
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_deleteInstance<'local>(
    _env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) {
    JniTypes::release_surreal(ptr);
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_connect<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    addr: JString<'local>,
) -> jboolean {
    with_env_body!(env, env, {
        let surreal = get_surreal_ref!(env, ptr, || false as jboolean);
        let addr = get_rust_string!(env, addr, || false as jboolean);
        // Connect
        if let Err(err) = crate::block_on(async move { surreal.connect(addr).await }) {
            return SurrealError::from(err).exception(env, || false as jboolean);
        }
        true as jboolean
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_version<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    with_env_body!(env, env, {
        let surreal = get_surreal_ref!(env, ptr, null_mut);
        let version = match crate::block_on(async move { surreal.version().await }) {
            Ok(v) => v.to_string(),
            // Embedded / local: no server to ask; report library version (injected at build time)
            Err(_) => env!("SURREALDB_VERSION").into(),
        };
        new_string!(env, version, null_mut)
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_health<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let surreal = get_surreal_ref!(env, ptr, || false as jboolean);
        match crate::block_on(async move { surreal.health().await }) {
            Ok(()) => true as jboolean,
            Err(e) => SurrealError::from(e).exception(env, || false as jboolean),
        }
    })
}

fn new_token_object<'local>(
    env: &mut Env<'local>,
    access: String,
    refresh: Option<String>,
) -> StdResult<jobject, SurrealError> {
    let token_class = env
        .find_class(jni_str!("com/surrealdb/signin/Token"))
        .map_err(SurrealError::from)?;
    let access_jstr = env.new_string(access).map_err(SurrealError::from)?;
    let refresh_jobj: JObject<'local> = match refresh {
        Some(s) => env.new_string(s).map_err(SurrealError::from)?.into(),
        None => JObject::null(),
    };
    let args = [
        JValue::Object(access_jstr.as_ref()),
        JValue::Object(refresh_jobj.as_ref()),
    ];
    let token_obj = env
        .new_object(
            token_class,
            jni_sig!("(Ljava/lang/String;Ljava/lang/String;)V"),
            &args,
        )
        .map_err(SurrealError::from)?;
    Ok(token_obj.into_raw())
}

fn new_ns_db_object<'local>(
    env: &mut Env<'local>,
    namespace: Option<String>,
    database: Option<String>,
) -> StdResult<jobject, SurrealError> {
    let ns_db_class = env
        .find_class(jni_str!("com/surrealdb/NsDb"))
        .map_err(SurrealError::from)?;
    let ns_jobj: JObject<'local> = match namespace {
        Some(s) => env.new_string(s).map_err(SurrealError::from)?.into(),
        None => JObject::null(),
    };
    let db_jobj: JObject<'local> = match database {
        Some(s) => env.new_string(s).map_err(SurrealError::from)?.into(),
        None => JObject::null(),
    };
    let args = [
        JValue::Object(ns_jobj.as_ref()),
        JValue::Object(db_jobj.as_ref()),
    ];
    let ns_db_obj = env
        .new_object(
            ns_db_class,
            jni_sig!("(Ljava/lang/String;Ljava/lang/String;)V"),
            &args,
        )
        .map_err(SurrealError::from)?;
    Ok(ns_db_obj.into_raw())
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_signinRoot<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    username: JString<'local>,
    password: JString<'local>,
) -> jobject {
    with_env_body!(env, env, {
        let surreal = get_surreal_ref!(env, ptr, null_mut);
        let username = get_rust_string!(env, username, null_mut);
        let password = get_rust_string!(env, password, null_mut);
        match crate::block_on(async move { surreal.signin(Root { username, password }).await }) {
            Ok(token) => {
                let access = token.access.into_insecure_token();
                let refresh = token.refresh.map(|r| r.into_insecure_token());
                match new_token_object(env, access, refresh) {
                    Ok(obj) => obj,
                    Err(e) => e.exception(env, null_mut),
                }
            }
            Err(err) => SurrealError::from(err).exception(env, null_mut),
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_signinNamespace<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    username: JString<'local>,
    password: JString<'local>,
    ns: JString<'local>,
) -> jobject {
    with_env_body!(env, env, {
        let surreal = get_surreal_ref!(env, ptr, null_mut);
        let username = get_rust_string!(env, username, null_mut);
        let password = get_rust_string!(env, password, null_mut);
        let namespace = get_rust_string!(env, ns, null_mut);
        match crate::block_on(async move {
            surreal
                .signin(Namespace {
                    username,
                    password,
                    namespace,
                })
                .await
        }) {
            Ok(token) => {
                let access = token.access.into_insecure_token();
                let refresh = token.refresh.map(|r| r.into_insecure_token());
                match new_token_object(env, access, refresh) {
                    Ok(obj) => obj,
                    Err(e) => e.exception(env, null_mut),
                }
            }
            Err(err) => SurrealError::from(err).exception(env, null_mut),
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_signinDatabase<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    username: JString<'local>,
    password: JString<'local>,
    ns: JString<'local>,
    db: JString<'local>,
) -> jobject {
    with_env_body!(env, env, {
        let surreal = get_surreal_ref!(env, ptr, null_mut);
        let username = get_rust_string!(env, username, null_mut);
        let password = get_rust_string!(env, password, null_mut);
        let namespace = get_rust_string!(env, ns, null_mut);
        let database = get_rust_string!(env, db, null_mut);
        match crate::block_on(async move {
            surreal
                .signin(Database {
                    username,
                    password,
                    namespace,
                    database,
                })
                .await
        }) {
            Ok(token) => {
                let access = token.access.into_insecure_token();
                let refresh = token.refresh.map(|r| r.into_insecure_token());
                match new_token_object(env, access, refresh) {
                    Ok(obj) => obj,
                    Err(e) => e.exception(env, null_mut),
                }
            }
            Err(err) => SurrealError::from(err).exception(env, null_mut),
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_signup<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    namespace: JString<'local>,
    database: JString<'local>,
    access: JString<'local>,
    params_value_ptr: jlong,
) -> jobject {
    with_env_body!(env, env, {
        let surreal = get_surreal_ref!(env, ptr, null_mut);
        let namespace = get_rust_string!(env, namespace, null_mut);
        let database = get_rust_string!(env, database, null_mut);
        let access = get_rust_string!(env, access, null_mut);
        let params = get_value_mut_instance!(env, params_value_ptr, null_mut).clone();
        let record = AuthRecord {
            namespace,
            database,
            access,
            params,
        };
        match crate::block_on(async move { surreal.signup(record).await }) {
            Ok(token) => {
                let access_str = token.access.into_insecure_token();
                let refresh = token.refresh.map(|r| r.into_insecure_token());
                match new_token_object(env, access_str, refresh) {
                    Ok(obj) => obj,
                    Err(e) => e.exception(env, null_mut),
                }
            }
            Err(err) => SurrealError::from(err).exception(env, null_mut),
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_signinRecord<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    namespace: JString<'local>,
    database: JString<'local>,
    access: JString<'local>,
    params_value_ptr: jlong,
) -> jobject {
    with_env_body!(env, env, {
        let surreal = get_surreal_ref!(env, ptr, null_mut);
        let namespace = get_rust_string!(env, namespace, null_mut);
        let database = get_rust_string!(env, database, null_mut);
        let access = get_rust_string!(env, access, null_mut);
        let params = get_value_mut_instance!(env, params_value_ptr, null_mut).clone();
        let record = AuthRecord {
            namespace,
            database,
            access,
            params,
        };
        match crate::block_on(async move { surreal.signin(record).await }) {
            Ok(token) => {
                let access_str = token.access.into_insecure_token();
                let refresh = token.refresh.map(|r| r.into_insecure_token());
                match new_token_object(env, access_str, refresh) {
                    Ok(obj) => obj,
                    Err(e) => e.exception(env, null_mut),
                }
            }
            Err(err) => SurrealError::from(err).exception(env, null_mut),
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_authenticate<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    token: JString<'local>,
) -> jboolean {
    with_env_body!(env, env, {
        let surreal = get_surreal_ref!(env, ptr, || false as jboolean);
        let token_str = get_rust_string!(env, token, || false as jboolean);
        if let Err(err) = crate::block_on(async move { surreal.authenticate(token_str).await }) {
            return SurrealError::from(err).exception(env, || false as jboolean);
        }
        true as jboolean
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_invalidate<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let surreal = get_surreal_ref!(env, ptr, || false as jboolean);
        if let Err(err) = crate::block_on(async move { surreal.invalidate().await }) {
            return SurrealError::from(err).exception(env, || false as jboolean);
        }
        true as jboolean
    })
}

/// Builds a crate token pair from the Java `Token`'s parts.
fn token_pair(access: String, refresh: String) -> surrealdb::opt::auth::Token {
    surrealdb::opt::auth::Token::from((
        surrealdb::opt::auth::AccessToken::from(access),
        surrealdb::opt::auth::RefreshToken::from(refresh),
    ))
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_refresh<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    access: JString<'local>,
    refresh: JString<'local>,
) -> jobject {
    with_env_body!(env, env, {
        let surreal = get_surreal_ref!(env, ptr, null_mut);
        let access = get_rust_string!(env, access, null_mut);
        let refresh = get_rust_string!(env, refresh, null_mut);
        let token = token_pair(access, refresh);
        // Exchanges the refresh token for a new pair and authenticates the session
        // with the new access token. The old refresh token is consumed.
        match crate::block_on(async move { surreal.authenticate(token).refresh().await }) {
            Ok(token) => {
                let access = token.access.into_insecure_token();
                let refresh = token.refresh.map(|r| r.into_insecure_token());
                match new_token_object(env, access, refresh) {
                    Ok(obj) => obj,
                    Err(e) => e.exception(env, null_mut),
                }
            }
            Err(err) => SurrealError::from(err).exception(env, null_mut),
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_revoke<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    access: JString<'local>,
    refresh: JString<'local>,
) {
    with_env_body!(env, env, {
        let surreal = get_surreal_ref!(env, ptr, || ());
        let access = get_rust_string!(env, access, || ());
        let refresh = get_rust_string!(env, refresh, || ());
        let token = token_pair(access, refresh);
        if let Err(err) = crate::block_on(async move { surreal.invalidate().refresh(token).await })
        {
            SurrealError::from(err).exception(env, || ())
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_set<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    name: JString<'local>,
    value_ptr: jlong,
) {
    with_env_body!(env, env, {
        let surreal = get_surreal_ref!(env, ptr, || ());
        let name = get_rust_string!(env, name, || ());
        let value = get_value_mut_instance!(env, value_ptr, || ()).clone();
        if let Err(err) = crate::block_on(async move { surreal.set(name, value).await }) {
            SurrealError::from(err).exception(env, || ())
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_unset<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    name: JString<'local>,
) {
    with_env_body!(env, env, {
        let surreal = get_surreal_ref!(env, ptr, || ());
        let name = get_rust_string!(env, name, || ());
        if let Err(err) = crate::block_on(async move { surreal.unset(name).await }) {
            SurrealError::from(err).exception(env, || ())
        }
    })
}

/// The record of the signed-in record user, as the `info` RPC returns it, or 0 when
/// the session is not authenticated as a record user.
#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_info<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        let surreal = get_surreal_ref!(env, ptr, || 0);
        let res = surrealdb_query::<()>(surreal, "RETURN $auth.*", None);
        let mut res = check_query_result!(env, res, || 0);
        match take_one_result!(env, res, || 0) {
            Value::None | Value::Null => 0,
            value => JniTypes::new_value(Arc::new(value)),
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_useNs<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    ns: JString<'local>,
) -> jobject {
    with_env_body!(env, env, {
        let surreal = get_surreal_ref!(env, ptr, null_mut);
        let ns = get_rust_string!(env, ns, null_mut);
        match crate::block_on(async move { surreal.use_ns(ns).await }) {
            Ok((namespace, database)) => match new_ns_db_object(env, namespace, database) {
                Ok(obj) => obj,
                Err(e) => e.exception(env, null_mut),
            },
            Err(err) => SurrealError::from(err).exception(env, null_mut),
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_useDb<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    db: JString<'local>,
) -> jobject {
    with_env_body!(env, env, {
        let surreal = get_surreal_ref!(env, ptr, null_mut);
        let db = get_rust_string!(env, db, null_mut);
        match crate::block_on(async move { surreal.use_db(db).await }) {
            Ok((namespace, database)) => match new_ns_db_object(env, namespace, database) {
                Ok(obj) => obj,
                Err(e) => e.exception(env, null_mut),
            },
            Err(err) => SurrealError::from(err).exception(env, null_mut),
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_useDefaults<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jobject {
    with_env_body!(env, env, {
        let surreal = get_surreal_ref!(env, ptr, null_mut);
        match crate::block_on(async move { surreal.use_defaults().await }) {
            Ok((namespace, database)) => match new_ns_db_object(env, namespace, database) {
                Ok(obj) => obj,
                Err(e) => e.exception(env, null_mut),
            },
            Err(err) => SurrealError::from(err).exception(env, null_mut),
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_query<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    query: JString<'local>,
) -> jlong {
    with_env_body!(env, env, {
        // Retrieve the Surreal instance
        let surreal = get_surreal_ref!(env, ptr, || 0);
        // Retrieve the query
        let query = get_rust_string!(env, &query, || 0);
        // Execute the query
        let res = surrealdb_query::<()>(surreal, &query, None);
        // Check the result
        let res = check_query_result!(env, res, || 0);
        // Build a response instance
        JniTypes::new_response(Arc::new(Mutex::new(res)))
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_queryWithBindings<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    query: JString<'local>,
    params_keys: JObjectArray<'local, JString<'local>>,
    params_values: JLongArray<'local>,
) -> jlong {
    with_env_body!(env, env, {
        let surreal = get_surreal_ref!(env, ptr, || 0);
        let query = get_rust_string!(env, &query, || 0);
        let params_map = build_params_map!(env, params_keys, params_values, || 0);
        let res = surrealdb_query::<Value>(surreal, &query, Some(params_map));
        let res = check_query_result!(env, res, || 0);
        JniTypes::new_response(Arc::new(Mutex::new(res)))
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_run<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    name: JString<'local>,
    args_value_ptrs: JLongArray<'local>,
) -> jlong {
    with_env_body!(env, env, {
        let surreal = get_surreal_ref!(env, ptr, || 0);
        let name = get_rust_string!(env, &name, || 0);
        if let Err(e) = check_function_name(&name) {
            return e.exception(env, || 0);
        }
        let value_ptrs = get_long_array!(env, &args_value_ptrs, || 0);
        let mut params_map = BTreeMap::<String, Value>::new();
        let mut placeholders = Vec::with_capacity(value_ptrs.len());
        for (i, value_ptr) in value_ptrs.iter().enumerate() {
            let key = format!("arg{}", i);
            placeholders.push(format!("${}", key));
            let value = get_value_mut_instance!(env, *value_ptr, || 0);
            params_map.insert(key, value.clone());
        }
        let args_list = placeholders.join(", ");
        let query = format!("RETURN {}({})", name, args_list);
        let res = surrealdb_query::<Value>(surreal, &query, Some(params_map));
        let mut response = check_query_result!(env, res, || 0);
        let mut result = take_one_result!(env, response, || 0);
        return_value_array_first!(result);
        // Single scalar result (e.g. RETURN fn::greet() returns the value directly, not [value])
        JniTypes::new_value(Arc::new(result))
    })
}

/// JNI implementation of `Surreal.selectLive(long ptr, String table, boolean diff)`.
///
/// Starts a live query on `table` and returns a fully-constructed Java
/// `LiveStream` object carrying both the native `LiveStreamChannel` handle and
/// the live-query UUID.
///
/// ## Architecture
///
/// ```text
///  Java thread                              Background thread       SurrealDB engine
///  ───────────                              ─────────────────       ────────────────
///  selectLive()
///    ├─ query("LIVE SELECT …").await ──────────────────────────▶  subscribe + UUID
///    ├─ res.take(0)   ──▶ live-query UUID (surfaces errors eagerly)
///    ├─ res.stream(0) ──▶ QueryStream<Value>
///    ├─ spawn ─────────────────────────────▶ loop { select! { notif ─▶ tx_thread.send() } }
///    ▼                                                │
///  new LiveStream(handle, uuid)                       │
///  nextNative()                                       │
///    rx.recv() ◀──────────────────────────────────────┘
/// ```
///
/// Unlike the `.select(table).live()` builder (whose query id is private), the
/// raw `LIVE SELECT` runs synchronously on the calling thread, so subscription
/// errors (e.g. the table does not exist) are surfaced eagerly via `take(0)`
/// rather than deferred to `next()`.  The statement returns a `Value::Uuid`,
/// which we read up front so it is available from `LiveStream.getQueryId()`
/// before any notification arrives.  A dedicated OS thread then drives the
/// notification stream on the shared tokio runtime, forwarding notifications
/// through an unbounded `async_channel` that the Java side reads via
/// `nextNative`.  `take(0)` and `stream(0)` read independent maps, so reading
/// the UUID does not consume the stream.
#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_selectLive<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    table: JString<'local>,
    diff: jboolean,
) -> jobject {
    with_env_body!(env, env, {
        let surreal = get_surreal_ref!(env, ptr, null_mut);
        let table = get_rust_string!(env, &table, null_mut);

        // Run the LIVE SELECT synchronously on the calling thread. The table name
        // is bound, never spliced into the query. In DIFF mode each notification
        // carries JSON Patch operations instead of the record.
        let mut params = BTreeMap::new();
        let tb = check_table_binding!(env, bind_table(&mut params, "tb", table), null_mut);
        let query = if diff {
            format!("LIVE SELECT DIFF FROM {tb}")
        } else {
            format!("LIVE SELECT * FROM {tb}")
        };
        let res = surrealdb_query::<Value>(surreal, &query, Some(params));
        let mut res = check_query_result!(env, res, null_mut);

        // The statement result at index 0 is the live-query UUID. Reading it via
        // take(0) also surfaces subscription errors (e.g. the table does not exist).
        let uuid_str = match res.take::<Value>(0) {
            Ok(Value::Uuid(uuid)) => uuid.to_string(),
            // Some servers/protocols return the live-query id wrapped in a one-element
            // array; unwrap it, mirroring the SDK's `.select(table).live()` builder.
            Ok(Value::Array(mut arr)) if arr.len() == 1 => match arr.pop() {
                Some(Value::Uuid(uuid)) => uuid.to_string(),
                other => {
                    return SurrealError::SurrealDBJni(format!(
                        "LIVE SELECT did not return a UUID: {other:?}"
                    ))
                    .exception(env, null_mut);
                }
            },
            Ok(other) => {
                return SurrealError::SurrealDBJni(format!(
                    "LIVE SELECT did not return a UUID: {}",
                    other.to_sql()
                ))
                .exception(env, null_mut);
            }
            Err(e) => return SurrealError::SurrealDB(e).exception(env, null_mut),
        };

        // The notification stream lives in a separate map from the results, so the
        // take(0) above does not disturb it.
        let qstream = match res.stream::<Value>(0) {
            Ok(s) => s,
            Err(e) => return SurrealError::SurrealDB(e).exception(env, null_mut),
        };

        // Notification channel: background thread produces, nextNative consumes.
        let (tx, rx) = async_channel::unbounded();
        // Shutdown channel: dropping shutdown_tx signals the background thread to exit.
        let (shutdown_tx, shutdown_rx) = async_channel::bounded::<()>(1);

        let tx_thread = tx.clone();
        let join_handle = std::thread::spawn(move || {
            TOKIO_RUNTIME.block_on(async move {
                let mut qstream = qstream;
                loop {
                    tokio::select! {
                        _ = shutdown_rx.recv() => break,
                        item = qstream.next() => match item {
                            Some(i) => {
                                let _ = tx_thread.send(i).await;
                            }
                            None => break,
                        },
                    }
                }
            });
        });

        let recv_mutex = std::sync::Arc::new(parking_lot::Mutex::new(()));
        let handle = JniTypes::new_live_stream((
            recv_mutex,
            parking_lot::Mutex::new(Some(join_handle)),
            parking_lot::Mutex::new(Some(shutdown_tx)),
            parking_lot::Mutex::new(Some(rx)),
        ));

        // Construct and return a LiveStream(handle, queryId), mirroring how live.rs
        // builds a LiveNotification.
        let uuid_raw = new_string!(env, uuid_str, null_mut);
        let uuid_jstr = unsafe { JObject::from_raw(env, uuid_raw) };
        let class = match env.find_class(jni_str!("com/surrealdb/LiveStream")) {
            Ok(c) => c,
            Err(e) => return SurrealError::from(e).exception(env, null_mut),
        };
        let args = [JValue::Long(handle), JValue::Object(&uuid_jstr)];
        match env.new_object(class, jni_sig!("(JLjava/lang/String;)V"), &args) {
            Ok(obj) => obj.into_raw(),
            Err(e) => SurrealError::from(e).exception(env, null_mut),
        }
    })
}

/// JNI implementation of `Surreal.kill(long ptr, String queryId)`.
///
/// Terminates the live query identified by `queryId` (a UUID string, e.g. from
/// `LiveStream.getQueryId()` or `LiveNotification.getQueryId()`) by running a
/// `KILL` statement on the same connection. After the server processes the
/// kill, the associated `LiveStream` ends and `LiveStream.next()` returns
/// empty. An invalid UUID or a connection failure is surfaced as a
/// `SurrealException`.
#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_kill<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    query_id: JString<'local>,
) {
    with_env_body!(env, env, {
        let surreal = get_surreal_ref!(env, ptr, || ());
        let query_id = get_rust_string!(env, &query_id, || ());
        let uuid = match Uuid::from_str(&query_id) {
            Ok(uuid) => uuid,
            Err(_) => {
                return SurrealError::SurrealDBJni(format!("Invalid live query id: {query_id}"))
                    .exception(env, || ());
            }
        };
        let mut params = BTreeMap::new();
        params.insert("id".to_string(), Value::Uuid(uuid));
        // The surrealdb client drops the result of every KILL statement
        // (`QueryType::Kill => {}` when it builds IndexedResults) and its typed kill is
        // `pub(crate)`, so a plain `KILL $id` cannot report a rejection. Running it in a
        // transaction makes a rejection observable: a failed KILL aborts the
        // transaction, so the COMMIT that follows returns an error ("Cannot COMMIT: the
        // transaction was aborted due to a prior error"). Results keep the index of their
        // statement even though KILL's is dropped: BEGIN=0, KILL=1 (dropped), COMMIT=2.
        // The client drops the KILL row, so its own error cannot be reached; a query
        // error naming the live query id is raised instead, with the aborted COMMIT as
        // its cause.
        let res = surrealdb_query::<Value>(surreal, "BEGIN; KILL $id; COMMIT;", Some(params));
        let mut res = check_query_result!(env, res, || ());
        if let Err(err) = res.take::<Value>(0) {
            return SurrealError::SurrealDB(err).exception(env, || ());
        }
        if let Err(err) = res.take::<Value>(2) {
            let err = surrealdb::Error::query(
                format!("Cannot execute KILL statement using id: u'{uuid}'"),
                None,
            )
            .with_cause(err);
            return SurrealError::SurrealDB(err).exception(env, || ());
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_exportSql<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    path: JString<'local>,
) -> jboolean {
    with_env_body!(env, env, {
        let surreal = get_surreal_ref!(env, ptr, || false as jboolean);
        let path = get_rust_string!(env, &path, || false as jboolean);
        let path = Path::new(&path).to_path_buf();
        match crate::block_on(async move { surreal.export(path).await }) {
            Ok(()) => true as jboolean,
            Err(e) => SurrealError::from(e).exception(env, || false as jboolean),
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_importSql<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    path: JString<'local>,
) -> jboolean {
    with_env_body!(env, env, {
        let surreal = get_surreal_ref!(env, ptr, || false as jboolean);
        let path = get_rust_string!(env, &path, || false as jboolean);
        let path = Path::new(&path).to_path_buf();
        match crate::block_on(async move { surreal.import(path).await }) {
            Ok(()) => true as jboolean,
            Err(e) => SurrealError::from(e).exception(env, || false as jboolean),
        }
    })
}

/// Binds `table` as the `$name` parameter holding a [`Table`] value and returns the
/// placeholder. Table names are never spliced into the query text, so a name such as
/// `person; REMOVE TABLE person` is just an (odd) table name, not SurrealQL.
fn bind_table(
    params: &mut BTreeMap<String, Value>,
    name: &str,
    table: String,
) -> StdResult<String, SurrealError> {
    check_table_name(&table)?;
    params.insert(name.to_string(), Value::Table(Table::new(table)));
    Ok(format!("${name}"))
}

/// Binds every table in `tables` as `$tb0`, `$tb1`, ... and returns the placeholders
/// joined with commas, ready for a `FROM` / `UPDATE` / `DELETE` target list.
fn bind_tables(
    params: &mut BTreeMap<String, Value>,
    tables: Vec<String>,
) -> StdResult<String, SurrealError> {
    if tables.is_empty() {
        return Err(SurrealError::SurrealDBJni(
            "At least one table name is required".to_string(),
        ));
    }
    let placeholders = tables
        .into_iter()
        .enumerate()
        .map(|(i, t)| bind_table(params, &format!("tb{i}"), t))
        .collect::<StdResult<Vec<_>, _>>()?;
    Ok(placeholders.join(", "))
}

/// Rejects an empty table name, and table names containing `:`. Such a string is
/// almost always a record id (`person:tobie`) passed where a table is expected.
/// Bound as a table it would silently create a new table called `person:tobie`, or
/// delete nothing, so it is refused with a pointer to [`RecordId`] instead.
fn check_table_name(table: &str) -> StdResult<(), SurrealError> {
    if table.is_empty() {
        Err(SurrealError::SurrealDBJni(
            "Invalid table name: the name is empty".to_string(),
        ))
    } else if table.contains(':') {
        Err(SurrealError::SurrealDBJni(format!(
            "Invalid table name: {table:?}. A String target is a table name and cannot contain ':'; use RecordId to address a single record"
        )))
    } else {
        Ok(())
    }
}

/// Checks that `name` is a plain SurrealQL function path (`fn::greet`, `string::len`,
/// `fn::my::nested`, optionally versioned as `ml::model<1.0.0>`). Function names cannot be bound as parameters, so anything that
/// is not a sequence of identifier segments joined by `::` is rejected rather than
/// spliced into the query.
fn check_function_name(name: &str) -> StdResult<(), SurrealError> {
    // An optional version suffix, e.g. `ml::model<1.0.0>`, is runs of digits joined by dots.
    let path = match name.strip_suffix('>').and_then(|n| n.split_once('<')) {
        Some((path, version))
            if version
                .split('.')
                .all(|seg| !seg.is_empty() && seg.chars().all(|c| c.is_ascii_digit())) =>
        {
            path
        }
        _ => name,
    };
    let valid = !path.is_empty()
        && path.split("::").all(|seg| {
            let mut chars = seg.chars();
            matches!(chars.next(), Some(c) if c.is_ascii_alphabetic() || c == '_')
                && chars.all(|c| c.is_ascii_alphanumeric() || c == '_')
        });
    if valid {
        Ok(())
    } else {
        Err(SurrealError::SurrealDBJni(format!(
            "Invalid function name: {name:?}"
        )))
    }
}

fn surrealdb_query<T>(
    surreal: &'static Surreal<Any>,
    query: impl Into<String>,
    params: Option<BTreeMap<String, T>>,
) -> Result<IndexedResults>
where
    T: SurrealValue + Serialize + Send + 'static,
{
    // The future is spawned on the Tokio runtime, so it must own the query.
    let query: String = query.into();
    crate::block_on(async move {
        let q = surreal.query(query);
        if let Some(p) = params {
            q.bind(p).await
        } else {
            q.await
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_createRecordIdValue<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    record_id_ptr: jlong,
    value_ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        // Retrieve the Surreal instance
        let surreal = get_surreal_ref!(env, surreal_ptr, || 0);
        // Extract the record id
        let record_id = get_value_instance!(env, record_id_ptr, || 0);
        // Get the value
        let value = get_value_mut_instance!(env, value_ptr, || 0);
        // Execute the query
        let params = BTreeMap::from([
            ("id".to_string(), record_id.as_ref().clone()),
            ("val".to_string(), value.clone()),
        ]);
        let query = "CREATE $id CONTENT $val";
        let res = surrealdb_query(surreal, query, Some(params));
        // Check the result
        let mut response = check_query_result!(env, res, || 0);
        // There is only one statement
        let mut result = take_one_result!(env, response, || 0);
        // There should be only one result
        return_value_array_first!(result);
        // Otherwise we return an error
        return_unexpected_result!(env, result.to_sql(), || 0)
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_createTargetValues<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    target: JString<'local>,
    value_ptrs: JLongArray<'local>,
) -> jlongArray {
    with_env_body!(env, env, {
        // Retrieve the Surreal instance
        let surreal = get_surreal_ref!(env, surreal_ptr, null_mut);
        // Build the parameters
        let target = get_rust_string!(env, target, null_mut);
        // Get the pointers
        let value_ptrs = get_long_array!(env, &value_ptrs, null_mut);
        // Build the queries
        let mut queries = Vec::with_capacity(value_ptrs.len());
        let mut params = BTreeMap::new();
        let tb = check_table_binding!(env, bind_table(&mut params, "tb", target), null_mut);
        if value_ptrs.is_empty() {
            return new_jlong_array!(env, &[] as &[jlong], null_mut);
        }
        for (idx, value_ptr) in value_ptrs.iter().enumerate() {
            queries.push(format!("CREATE {tb} CONTENT $i{idx}"));
            let value = get_value_mut_instance!(env, *value_ptr, null_mut);
            params.insert(format!("i{idx}"), value.clone());
        }
        let query = queries.join(";\n");
        // Execute the query
        let res = surrealdb_query(surreal, &query, Some(params));
        // Check the result
        let mut res = check_query_result!(env, res, null_mut);
        // Prepare the result
        let mut value_ptrs: Vec<jlong> = Vec::with_capacity(res.num_statements());
        // Iterate over the statement
        for i in 0..res.num_statements() {
            let mut res = match res.take::<Value>(i) {
                Ok(r) => r,
                Err(e) => return SurrealError::SurrealDB(e).exception(env, null_mut),
            };
            // There should be only one result per statement
            if let Value::Array(ref mut a) = res {
                if a.len() != 1 {
                    return SurrealError::SurrealDBJni(format!(
                        "Unexpected result: {}",
                        res.to_sql()
                    ))
                    .exception(env, null_mut);
                }
                let val = a.remove(0);
                let value_ptr = JniTypes::new_value(val.into());
                value_ptrs.push(value_ptr);
            }
        }
        new_jlong_array!(env, &value_ptrs, null_mut)
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_insertTargetValues<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    target: JString<'local>,
    value_ptrs: JLongArray<'local>,
) -> jlongArray {
    with_env_body!(env, env, {
        let target = get_rust_string!(env, target, null_mut);
        let value_ptrs = get_long_array!(env, &value_ptrs, null_mut);
        insert_target_values(env, surreal_ptr, target, &value_ptrs, false)
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_insertRelationTargetValue<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    target: JString<'local>,
    value_ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        // Retrieve the Surreal instance
        let surreal = get_surreal_ref!(env, surreal_ptr, || 0);
        // Build the parameters
        let target = get_rust_string!(env, target, || 0);
        // Get the value
        let value = get_value_mut_instance!(env, value_ptr, || 0);
        // Execute the query
        let mut params = BTreeMap::new();
        let tb = check_table_binding!(env, bind_table(&mut params, "tb", target), || 0);
        params.insert("data".to_string(), value.clone());
        let query = format!("INSERT RELATION INTO {tb} $data");
        let res = surrealdb_query(surreal, &query, Some(params));
        // Check the result
        let mut response = check_query_result!(env, res, || 0);
        // There is only one statement
        let mut result = take_one_result!(env, response, || 0);
        // There should be only one result
        return_value_array_first!(result);
        // Otherwise we return an error
        return_unexpected_result!(env, result.to_sql(), || 0)
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_insertRelationTargetValues<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    target: JString<'local>,
    value_ptrs: JLongArray<'local>,
) -> jlongArray {
    with_env_body!(env, env, {
        let target = get_rust_string!(env, target, null_mut);
        let value_ptrs = get_long_array!(env, &value_ptrs, null_mut);
        insert_target_values(env, surreal_ptr, target, &value_ptrs, true)
    })
}

/// Inserts `value_ptrs` into `target` with `INSERT INTO`, or `INSERT RELATION INTO`
/// when `relation` is set, and returns the inserted records. No records is
/// nothing to insert, so nothing is sent.
fn insert_target_values(
    env: &mut Env,
    surreal_ptr: jlong,
    target: String,
    value_ptrs: &[jlong],
    relation: bool,
) -> jlongArray {
    // Retrieve the Surreal instance
    let surreal = get_surreal_ref!(env, surreal_ptr, null_mut);
    let mut params = BTreeMap::new();
    let tb = check_table_binding!(env, bind_table(&mut params, "tb", target), null_mut);
    if value_ptrs.is_empty() {
        return new_jlong_array!(env, &[] as &[jlong], null_mut);
    }
    // Build the query
    let mut records = Vec::with_capacity(value_ptrs.len());
    for value_ptr in value_ptrs {
        let value = get_value_mut_instance!(env, *value_ptr, null_mut);
        records.push(value.clone());
    }
    params.insert("data".to_string(), Value::Array(Array::from(records)));
    let relation = if relation { "RELATION " } else { "" };
    let query = format!("INSERT {relation}INTO {tb} $data");
    // Execute the query
    let res = surrealdb_query(surreal, &query, Some(params));
    // Check the result
    let mut response = check_query_result!(env, res, null_mut);
    // There is only one statement
    let result = take_one_result!(env, response, null_mut);
    if let Value::Array(a) = result {
        // Prepare the result
        let mut value_ptrs: Vec<jlong> = Vec::with_capacity(a.len());
        for val in a.into_iter() {
            let value_ptr = JniTypes::new_value(val.into());
            value_ptrs.push(value_ptr);
        }
        new_jlong_array!(env, &value_ptrs, null_mut)
    } else {
        SurrealError::SurrealDBJni(format!("Unexpected result: {}", result.to_sql()))
            .exception(env, null_mut)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_relate<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    from_ptr: jlong,
    target: JString<'local>,
    to_ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        // Retrieve the Surreal instance
        let surreal = get_surreal_ref!(env, surreal_ptr, || 0);
        // Build the parameters
        let target = get_rust_string!(env, target, || 0);
        // Get from and to
        let from_value = get_value_instance!(env, from_ptr, || 0);
        let to_value = get_value_instance!(env, to_ptr, || 0);
        // Execute the query
        let mut params = BTreeMap::new();
        let tb = check_table_binding!(env, bind_table(&mut params, "tb", target), || 0);
        params.insert("from".to_string(), from_value.as_ref().clone());
        params.insert("to".to_string(), to_value.as_ref().clone());
        let query = format!("RELATE $from->{tb}->$to");
        let res = surrealdb_query(surreal, &query, Some(params));
        // Check the result
        let mut response = check_query_result!(env, res, || 0);
        // There is only one statement
        let mut result = take_one_result!(env, response, || 0);
        // There should be only one result
        return_value_array_first!(result);
        // Otherwise we return an error
        return_unexpected_result!(env, result.to_sql(), || 0)
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_relateContent<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    from_ptr: jlong,
    target: JString<'local>,
    to_ptr: jlong,
    content_ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        // Retrieve the Surreal instance
        let surreal = get_surreal_ref!(env, surreal_ptr, || 0);
        // Build the parameters
        let target = get_rust_string!(env, target, || 0);
        // Get from and to
        let from_value = get_value_instance!(env, from_ptr, || 0);
        let to_value = get_value_instance!(env, to_ptr, || 0);
        let content_value = get_value_mut_instance!(env, content_ptr, || 0);
        // Execute the query
        let mut params = BTreeMap::new();
        let tb = check_table_binding!(env, bind_table(&mut params, "tb", target), || 0);
        params.insert("from".to_string(), from_value.as_ref().clone());
        params.insert("to".to_string(), to_value.as_ref().clone());
        params.insert("content".to_string(), content_value.clone());
        let query = format!("RELATE $from->{tb}->$to CONTENT $content");
        let res = surrealdb_query(surreal, &query, Some(params));
        // Check the result
        let mut response = check_query_result!(env, res, || 0);
        // There is only one statement
        let mut result = take_one_result!(env, response, || 0);
        // There should be only one result
        return_value_array_first!(result);
        // Otherwise we return an error
        return_unexpected_result!(env, result.to_sql(), || 0)
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_selectRecordId<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    record_id_ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        // Retrieve the Surreal instance
        let surreal = get_surreal_ref!(env, surreal_ptr, || 0);
        // Extract the record id
        let record_id = get_value_instance!(env, record_id_ptr, || 0);
        // Execute the query
        let params = BTreeMap::from([("id".to_string(), record_id)]);
        let res = surrealdb_query(surreal, "SELECT * FROM $id", Some(params));
        // Check the result
        let mut res = check_query_result!(env, res, || 0);
        // There is only one statement
        let mut res = take_one_result!(env, res, || 0);
        // There should be only one result
        return_value_array_first!(res);
        // If the array is empty, return null (0) for Optional.empty()
        if let Value::Array(ref a) = res {
            if a.is_empty() {
                return 0;
            }
        }
        // Otherwise throw an error
        return_unexpected_result!(env, res.to_sql(), || 0)
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_selectRecordIds<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    record_id_ptrs: JLongArray<'local>,
) -> jlongArray {
    with_env_body!(env, env, {
        // Retrieve the Surreal instance
        let surreal = get_surreal_ref!(env, surreal_ptr, null_mut);
        // Get the record id pointers
        let record_id_ptrs = get_long_array!(env, &record_id_ptrs, null_mut);
        if record_id_ptrs.is_empty() {
            return new_jlong_array!(env, &[] as &[jlong], null_mut);
        }
        // Extract the record ids
        let mut record_ids = Vec::with_capacity(record_id_ptrs.len());
        let mut params = BTreeMap::new();
        for (idx, record_id_ptr) in record_id_ptrs.into_iter().enumerate() {
            let record_id = get_value_instance!(env, record_id_ptr, null_mut);
            params.insert(format!("id{idx}"), record_id);
            record_ids.push(format!("$id{idx}"));
        }
        // Execute the query
        let query = format!("SELECT * FROM {}", record_ids.join(", "));
        let res = surrealdb_query(surreal, &query, Some(params));
        // Check the result
        let mut res = check_query_result!(env, res, null_mut);
        // There is only one statement
        let res = take_one_result!(env, res, null_mut);
        // Prepare the result
        if let Value::Array(a) = res {
            let mut value_ptrs: Vec<jlong> = Vec::with_capacity(a.len());
            for value in a {
                let value_ptr = JniTypes::new_value(Arc::new(value));
                value_ptrs.push(value_ptr);
            }
            // Return the results
            new_jlong_array!(env, &value_ptrs, null_mut)
        } else {
            SurrealError::SurrealDBJni(format!("Unexpected result: {}", res.to_sql()))
                .exception(env, null_mut)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_selectTargetsValues<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    targets: JObjectArray<'local, JString<'local>>,
) -> jlong {
    with_env_body!(env, env, {
        let targets = get_rust_string_array!(env, targets, || 0);
        select_targets_values(env, surreal_ptr, targets, false)
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_selectTargetsValuesSync<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    targets: JObjectArray<'local, JString<'local>>,
) -> jlong {
    with_env_body!(env, env, {
        let targets = get_rust_string_array!(env, targets, || 0);
        select_targets_values(env, surreal_ptr, targets, true)
    })
}

fn select_targets_values(
    env: &mut Env,
    surreal_ptr: jlong,
    targets: Vec<String>,
    sync: bool,
) -> jlong {
    // Retrieve the Surreal instance
    let surreal = get_surreal_ref!(env, surreal_ptr, || 0);
    // Prepare the query
    let mut params = BTreeMap::new();
    let tbs = check_table_binding!(env, bind_tables(&mut params, targets), || 0);
    let query = format!("SELECT * FROM {tbs}");
    // Execute the query
    let res = surrealdb_query(surreal, &query, Some(params));
    // Check the result
    let mut response = check_query_result!(env, res, || 0);
    // There is only one statement
    let result = take_one_result!(env, response, || 0);
    rows_iter(env, result, sync)
}

/// Returns the rows of `result` as a value iterator, a synchronized one if `sync`
/// is set, or throws if `result` is not an array.
fn rows_iter(env: &mut Env, result: Value, sync: bool) -> jlong {
    if sync {
        return_value_array_iter_sync!(result);
    } else {
        return_value_array_iter!(result);
    }
    return_unexpected_result!(env, result.to_sql(), || 0)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_deleteRecordId<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    record_id_ptr: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        // Retrieve the Surreal instance
        let surreal = get_surreal_ref!(env, surreal_ptr, || false as jboolean);
        // Build the parameters
        let record_id = get_value_instance!(env, record_id_ptr, || false as jboolean);
        // Prepare the params
        let params = BTreeMap::from([("t".to_string(), record_id)]);
        // Execute the query
        let res = surrealdb_query(surreal, "DELETE $t", Some(params));
        // Check the result
        check_query_result!(env, res, || false as jboolean);
        true as jboolean
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_deleteRecordIds<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    record_id_ptrs: JLongArray<'local>,
) -> jboolean {
    with_env_body!(env, env, {
        // Retrieve the Surreal instance
        let surreal = get_surreal_ref!(env, surreal_ptr, || false as jboolean);
        // Extract the record ids
        let record_id_ptrs = get_long_array!(env, &record_id_ptrs, || false as jboolean);
        if record_id_ptrs.is_empty() {
            return true as jboolean;
        }
        // Prepare the params
        let mut targets = Vec::with_capacity(record_id_ptrs.len());
        let mut params = BTreeMap::new();
        for (idx, record_id_ptr) in record_id_ptrs.iter().enumerate() {
            let value = get_value_instance!(env, *record_id_ptr, || false as jboolean);
            params.insert(format!("t{idx}"), value);
            targets.push(format!("$t{idx}"));
        }
        // Prepare the query
        let query = format!("DELETE {}", targets.join(","));
        // Execute the query
        let res = surrealdb_query(surreal, &query, Some(params));
        // Check the result
        check_query_result!(env, res, || false);
        true as jboolean
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_selectRecordIdRange<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    table: JString<'local>,
    start_id_ptr: jlong,
    end_id_ptr: jlong,
    bounds: jint,
) -> jlongArray {
    with_env_body!(env, env, {
        let surreal = get_surreal_ref!(env, surreal_ptr, null_mut);
        let table_str = get_rust_string!(env, table, null_mut);
        let range_value = match build_range_value(env, &table_str, start_id_ptr, end_id_ptr, bounds)
        {
            Ok(v) => v,
            Err(e) => return e.exception(env, null_mut),
        };
        let params = BTreeMap::from([("_range".to_string(), range_value)]);
        let res = surrealdb_query(surreal, "SELECT * FROM $_range", Some(params));
        let mut res = check_query_result!(env, res, null_mut);
        let res = take_one_result!(env, res, null_mut);
        if let Value::Array(a) = res {
            let mut value_ptrs: Vec<jlong> = Vec::with_capacity(a.len());
            for value in a {
                let value_ptr = JniTypes::new_value(Arc::new(value));
                value_ptrs.push(value_ptr);
            }
            new_jlong_array!(env, &value_ptrs, null_mut)
        } else {
            SurrealError::SurrealDBJni(format!("Unexpected result: {}", res.to_sql()))
                .exception(env, null_mut)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_deleteRecordIdRange<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    table: JString<'local>,
    start_id_ptr: jlong,
    end_id_ptr: jlong,
    bounds: jint,
) -> jboolean {
    with_env_body!(env, env, {
        let surreal = get_surreal_ref!(env, surreal_ptr, || false as jboolean);
        let table_str = get_rust_string!(env, table, || false as jboolean);
        let range_value = match build_range_value(env, &table_str, start_id_ptr, end_id_ptr, bounds)
        {
            Ok(v) => v,
            Err(e) => return e.exception(env, || false as jboolean),
        };
        let params = BTreeMap::from([("_range".to_string(), range_value)]);
        let res = surrealdb_query(surreal, "DELETE $_range", Some(params));
        check_query_result!(env, res, || false as jboolean);
        true as jboolean
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_deleteTarget<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    target: JString<'local>,
) -> jboolean {
    with_env_body!(env, env, {
        // Retrieve the Surreal instance
        let surreal = get_surreal_ref!(env, surreal_ptr, || false as jboolean);
        // Get the targets
        let target = get_rust_string!(env, target, || false as jboolean);
        // Prepare the query
        let mut params = BTreeMap::new();
        let tb = check_table_binding!(env, bind_table(&mut params, "tb", target), || false
            as jboolean);
        let query = format!("DELETE {tb}");
        // Execute the query
        let res = surrealdb_query(surreal, &query, Some(params));
        // Check the result
        check_query_result!(env, res, || false as jboolean);
        true as jboolean
    })
}

/// Converts a Value (e.g. from an Id) to RecordIdKey for range bounds.
fn value_to_record_id_key(value: &Value) -> std::result::Result<RecordIdKey, SurrealError> {
    if let Value::RecordId(rid) = value {
        Ok(rid.key.clone())
    } else {
        RecordIdKey::from_value(value.clone())
            .map_err(|e| SurrealError::SurrealDBJni(e.to_string()))
    }
}

/// `bounds` flag (mirrors RecordIdRange.START_EXCLUSIVE): the start bound is exclusive.
const RANGE_START_EXCLUSIVE: jint = 1;
/// `bounds` flag (mirrors RecordIdRange.END_EXCLUSIVE): the end bound is exclusive.
const RANGE_END_EXCLUSIVE: jint = 2;

fn range_bound(
    ptr: jlong,
    exclusive: bool,
) -> std::result::Result<Bound<RecordIdKey>, SurrealError> {
    if ptr == 0 {
        return Ok(Bound::Unbounded);
    }
    let value = crate::get_instance::<crate::ValueHandle>(ptr, crate::JniTypes::Value)?;
    let key = value_to_record_id_key(value.as_ref())?;
    Ok(if exclusive {
        Bound::Excluded(key)
    } else {
        Bound::Included(key)
    })
}

/// Builds the range record id value for SELECT/DELETE/UPDATE/UPSERT $_range.
fn build_range_value(
    _env: &mut Env,
    table: &str,
    start_ptr: jlong,
    end_ptr: jlong,
    bounds: jint,
) -> std::result::Result<Value, SurrealError> {
    let start_bound = range_bound(start_ptr, bounds & RANGE_START_EXCLUSIVE != 0)?;
    let end_bound = range_bound(end_ptr, bounds & RANGE_END_EXCLUSIVE != 0)?;
    let range = RecordIdKeyRange {
        start: start_bound,
        end: end_bound,
    };
    let range_record_id = RecordId::new(table.to_string(), RecordIdKey::Range(Box::new(range)));
    Ok(Value::RecordId(range_record_id))
}

fn up_record_id_value(
    env: &mut Env,
    surreal_ptr: jlong,
    record_id_ptr: jlong,
    up_type: jint,
    value_ptr: jlong,
    up: &str,
) -> jlong {
    // Retrieve the Surreal instance
    let surreal = get_surreal_ref!(env, surreal_ptr, || 0);
    // Extract the record id
    let record_id = get_value_instance!(env, record_id_ptr, || 0);
    // Get the value
    let value = get_value_mut_instance!(env, value_ptr, || 0);
    // Check the up type
    let up_type = convert_up_type!(env, up_type, || 0);
    // Execute the query
    let query = format!("{up} $id {up_type} $val");
    let params = BTreeMap::from([
        ("id".to_string(), record_id.as_ref().clone()),
        ("val".to_string(), value.clone()),
    ]);
    let res = surrealdb_query(surreal, &query, Some(params));
    // Check the result
    let mut response = check_query_result!(env, res, || 0);
    // There is only one statement
    let mut result: Value = take_one_result!(env, response, || 0);
    // There should be only one result
    return_value_array_first!(result);
    // Otherwise we return an error
    return_unexpected_result!(env, result.to_sql(), || 0)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_updateRecordIdValue<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    record_id_ptr: jlong,
    up_type: jint,
    value_ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        up_record_id_value(
            env,
            surreal_ptr,
            record_id_ptr,
            up_type,
            value_ptr,
            "update",
        )
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_upsertRecordIdValue<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    record_id_ptr: jlong,
    up_type: jint,
    value_ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        up_record_id_value(
            env,
            surreal_ptr,
            record_id_ptr,
            up_type,
            value_ptr,
            "upsert",
        )
    })
}

#[allow(clippy::too_many_arguments)]
fn up_record_id_range_value(
    env: &mut Env,
    surreal_ptr: jlong,
    table: JString,
    start_id_ptr: jlong,
    end_id_ptr: jlong,
    bounds: jint,
    up_type: jint,
    value_ptr: jlong,
    up: &str,
) -> jlong {
    let surreal = get_surreal_ref!(env, surreal_ptr, || 0);
    let table_str = get_rust_string!(env, table, || 0);
    let range_value = match build_range_value(env, &table_str, start_id_ptr, end_id_ptr, bounds) {
        Ok(v) => v,
        Err(e) => return e.exception(env, || 0),
    };
    let value = get_value_mut_instance!(env, value_ptr, || 0);
    let up_type = convert_up_type!(env, up_type, || 0);
    let mut params = BTreeMap::new();
    params.insert("_range".to_string(), range_value);
    params.insert("val".to_string(), value.clone());
    let query = format!("{up} $_range {up_type} $val");
    let res = surrealdb_query(surreal, &query, Some(params));
    let mut response = check_query_result!(env, res, || 0);
    let mut result: Value = take_one_result!(env, response, || 0);
    return_value_array_first!(result);
    // Range update/upsert can return [] or [one or more records]; return first or None
    if let Value::Array(ref mut a) = result {
        if a.is_empty() {
            return JniTypes::new_value(Arc::new(Value::None));
        }
        return JniTypes::new_value(Arc::new(a.remove(0)));
    }
    return_unexpected_result!(env, result.to_sql(), || 0)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_updateRecordIdRangeValue<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    table: JString<'local>,
    start_id_ptr: jlong,
    end_id_ptr: jlong,
    bounds: jint,
    up_type: jint,
    value_ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        up_record_id_range_value(
            env,
            surreal_ptr,
            table,
            start_id_ptr,
            end_id_ptr,
            bounds,
            up_type,
            value_ptr,
            "update",
        )
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_upsertRecordIdRangeValue<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    table: JString<'local>,
    start_id_ptr: jlong,
    end_id_ptr: jlong,
    bounds: jint,
    up_type: jint,
    value_ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        up_record_id_range_value(
            env,
            surreal_ptr,
            table,
            start_id_ptr,
            end_id_ptr,
            bounds,
            up_type,
            value_ptr,
            "upsert",
        )
    })
}

fn up_target_value(
    env: &mut Env,
    surreal_ptr: jlong,
    targets: Vec<String>,
    up_type: jint,
    value_ptr: jlong,
    up: &str,
    sync: bool,
) -> jlong {
    // Retrieve the Surreal instance
    let surreal = get_surreal_ref!(env, surreal_ptr, || 0);
    // Check the up type and the targets before copying the value
    let up_type = convert_up_type!(env, up_type, || 0);
    let mut params = BTreeMap::new();
    let tbs = check_table_binding!(env, bind_tables(&mut params, targets), || 0);
    // Get the value
    let value = get_value_mut_instance!(env, value_ptr, || 0);
    params.insert("val".to_string(), value.clone());
    // Execute the query
    let query = format!("{up} {tbs} {up_type} $val");
    let res = surrealdb_query(surreal, &query, Some(params));
    // Check the result
    let mut response = check_query_result!(env, res, || 0);
    // There is only one statement
    let result: Value = take_one_result!(env, response, || 0);
    rows_iter(env, result, sync)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_updateTargetValue<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    target: JString<'local>,
    up_type: jint,
    value_ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        let target = get_rust_string!(env, target, || 0);
        up_target_value(
            env,
            surreal_ptr,
            vec![target],
            up_type,
            value_ptr,
            "update",
            false,
        )
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_updateTargetsValue<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    targets: JObjectArray<'local, JString<'local>>,
    up_type: jint,
    value_ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        let targets = get_rust_string_array!(env, targets, || 0);
        up_target_value(
            env,
            surreal_ptr,
            targets,
            up_type,
            value_ptr,
            "update",
            false,
        )
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_upsertTargetValue<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    target: JString<'local>,
    up_type: jint,
    value_ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        let target = get_rust_string!(env, target, || 0);
        up_target_value(
            env,
            surreal_ptr,
            vec![target],
            up_type,
            value_ptr,
            "upsert",
            false,
        )
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_upsertTargetsValue<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    targets: JObjectArray<'local, JString<'local>>,
    up_type: jint,
    value_ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        let targets = get_rust_string_array!(env, targets, || 0);
        up_target_value(
            env,
            surreal_ptr,
            targets,
            up_type,
            value_ptr,
            "upsert",
            false,
        )
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_updateTargetValueSync<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    target: JString<'local>,
    up_type: jint,
    value_ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        let target = get_rust_string!(env, target, || 0);
        up_target_value(
            env,
            surreal_ptr,
            vec![target],
            up_type,
            value_ptr,
            "update",
            true,
        )
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_updateTargetsValueSync<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    targets: JObjectArray<'local, JString<'local>>,
    up_type: jint,
    value_ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        let targets = get_rust_string_array!(env, targets, || 0);
        up_target_value(
            env,
            surreal_ptr,
            targets,
            up_type,
            value_ptr,
            "update",
            true,
        )
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_upsertTargetValueSync<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    target: JString<'local>,
    up_type: jint,
    value_ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        let target = get_rust_string!(env, target, || 0);
        up_target_value(
            env,
            surreal_ptr,
            vec![target],
            up_type,
            value_ptr,
            "upsert",
            true,
        )
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_upsertTargetsValueSync<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    targets: JObjectArray<'local, JString<'local>>,
    up_type: jint,
    value_ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        let targets = get_rust_string_array!(env, targets, || 0);
        up_target_value(
            env,
            surreal_ptr,
            targets,
            up_type,
            value_ptr,
            "upsert",
            true,
        )
    })
}
