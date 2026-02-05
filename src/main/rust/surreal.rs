use std::collections::BTreeMap;
use std::ptr::null_mut;
use std::sync::Arc;

use crate::error::SurrealError;
use crate::{
    check_query_result, convert_up_type, get_long_array, get_rust_string,
    get_rust_string_array, get_surreal_ref, get_value_instance,
    get_value_mut_instance, new_jlong_array, release_instance, return_unexpected_result,
    return_value_array_first, return_value_array_iter, return_value_array_iter_sync,
    take_one_result, JniTypes, TOKIO_RUNTIME,
};
use jni::objects::{JClass, JObject, JLongArray, JObjectArray, JString, JValue};
use jni::sys::{jboolean, jint, jlong, jlongArray, jobject};
use jni::JNIEnv;
use std::result::Result as StdResult;
use parking_lot::Mutex;
use serde::Serialize;
use surrealdb::engine::any::Any;
use surrealdb::opt::auth::{Database, Namespace, Record as AuthRecord, Root};
use surrealdb::types::{SurrealValue, ToSql, Value};
use surrealdb::{IndexedResults, Result, Surreal};

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_beginTransaction<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    let surreal = get_surreal_ref!(&mut env, ptr, || 0);
    match TOKIO_RUNTIME.block_on(async {
        surreal.clone().begin().await
    }) {
        Ok(txn) => JniTypes::new_transaction(txn),
        Err(e) => SurrealError::from(e).exception(&mut env, || 0),
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_cloneSession<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    match crate::clone_surreal_instance(ptr) {
        Ok(new_ptr) => new_ptr,
        Err(e) => e.exception(&mut env, || 0),
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_newInstance<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
) -> jlong {
    JniTypes::new_surreal(Surreal::<Any>::init())
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_deleteInstance<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) {
    release_instance::<Surreal<Any>>(ptr);
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_connect<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    addr: JString<'local>,
) -> jboolean {
    let surreal = get_surreal_ref!(&mut env, ptr, || false as jboolean);
    let addr = get_rust_string!(env, addr, || false as jboolean);
    // Connect
    if let Err(err) = TOKIO_RUNTIME.block_on(async { surreal.connect(addr).await }) {
        return SurrealError::from(err).exception(&mut env, || false as jboolean);
    }
    true as jboolean
}

fn new_token_object<'local>(
    env: &mut JNIEnv<'local>,
    access: String,
    refresh: Option<String>,
) -> StdResult<jobject, SurrealError> {
    let token_class = env.find_class("com/surrealdb/signin/Token").map_err(SurrealError::from)?;
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
        .new_object(token_class, "(Ljava/lang/String;Ljava/lang/String;)V", &args)
        .map_err(SurrealError::from)?;
    Ok(token_obj.into_raw())
}

fn new_ns_db_object<'local>(
    env: &mut JNIEnv<'local>,
    namespace: Option<String>,
    database: Option<String>,
) -> StdResult<jobject, SurrealError> {
    let ns_db_class = env.find_class("com/surrealdb/NsDb").map_err(SurrealError::from)?;
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
        .new_object(ns_db_class, "(Ljava/lang/String;Ljava/lang/String;)V", &args)
        .map_err(SurrealError::from)?;
    Ok(ns_db_obj.into_raw())
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_signinRoot<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    username: JString<'local>,
    password: JString<'local>,
) -> jobject {
    let surreal = get_surreal_ref!(&mut env, ptr, null_mut);
    let username = get_rust_string!(&mut env, username, null_mut);
    let password = get_rust_string!(&mut env, password, null_mut);
    match TOKIO_RUNTIME.block_on(async {
        surreal
            .signin(Root {
                username,
                password,
            })
            .await
    }) {
        Ok(token) => {
            let access = token.access.into_insecure_token();
            let refresh = token.refresh.map(|r| r.into_insecure_token());
            match new_token_object(&mut env, access, refresh) {
                Ok(obj) => obj,
                Err(e) => SurrealError::from(e).exception(&mut env, null_mut),
            }
        }
        Err(err) => SurrealError::from(err).exception(&mut env, null_mut),
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_signinNamespace<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    username: JString<'local>,
    password: JString<'local>,
    ns: JString<'local>,
) -> jobject {
    let surreal = get_surreal_ref!(&mut env, ptr, null_mut);
    let username = get_rust_string!(&mut env, username, null_mut);
    let password = get_rust_string!(&mut env, password, null_mut);
    let namespace = get_rust_string!(&mut env, ns, null_mut);
    match TOKIO_RUNTIME.block_on(async {
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
            match new_token_object(&mut env, access, refresh) {
                Ok(obj) => obj,
                Err(e) => SurrealError::from(e).exception(&mut env, null_mut),
            }
        }
        Err(err) => SurrealError::from(err).exception(&mut env, null_mut),
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_signinDatabase<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    username: JString<'local>,
    password: JString<'local>,
    ns: JString<'local>,
    db: JString<'local>,
) -> jobject {
    let surreal = get_surreal_ref!(&mut env, ptr, null_mut);
    let username = get_rust_string!(&mut env, username, null_mut);
    let password = get_rust_string!(&mut env, password, null_mut);
    let namespace = get_rust_string!(&mut env, ns, null_mut);
    let database = get_rust_string!(&mut env, db, null_mut);
    match TOKIO_RUNTIME.block_on(async {
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
            match new_token_object(&mut env, access, refresh) {
                Ok(obj) => obj,
                Err(e) => SurrealError::from(e).exception(&mut env, null_mut),
            }
        }
        Err(err) => SurrealError::from(err).exception(&mut env, null_mut),
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_signup<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    namespace: JString<'local>,
    database: JString<'local>,
    access: JString<'local>,
    params_value_ptr: jlong,
) -> jobject {
    let surreal = get_surreal_ref!(&mut env, ptr, null_mut);
    let namespace = get_rust_string!(&mut env, namespace, null_mut);
    let database = get_rust_string!(&mut env, database, null_mut);
    let access = get_rust_string!(&mut env, access, null_mut);
    let params = get_value_mut_instance!(&mut env, params_value_ptr, null_mut).clone();
    let record = AuthRecord {
        namespace,
        database,
        access,
        params,
    };
    match TOKIO_RUNTIME.block_on(async { surreal.signup(record).await }) {
        Ok(token) => {
            let access_str = token.access.into_insecure_token();
            let refresh = token.refresh.map(|r| r.into_insecure_token());
            match new_token_object(&mut env, access_str, refresh) {
                Ok(obj) => obj,
                Err(e) => SurrealError::from(e).exception(&mut env, null_mut),
            }
        }
        Err(err) => SurrealError::from(err).exception(&mut env, null_mut),
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_signinRecord<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    namespace: JString<'local>,
    database: JString<'local>,
    access: JString<'local>,
    params_value_ptr: jlong,
) -> jobject {
    let surreal = get_surreal_ref!(&mut env, ptr, null_mut);
    let namespace = get_rust_string!(&mut env, namespace, null_mut);
    let database = get_rust_string!(&mut env, database, null_mut);
    let access = get_rust_string!(&mut env, access, null_mut);
    let params = get_value_mut_instance!(&mut env, params_value_ptr, null_mut).clone();
    let record = AuthRecord {
        namespace,
        database,
        access,
        params,
    };
    match TOKIO_RUNTIME.block_on(async { surreal.signin(record).await }) {
        Ok(token) => {
            let access_str = token.access.into_insecure_token();
            let refresh = token.refresh.map(|r| r.into_insecure_token());
            match new_token_object(&mut env, access_str, refresh) {
                Ok(obj) => obj,
                Err(e) => SurrealError::from(e).exception(&mut env, null_mut),
            }
        }
        Err(err) => SurrealError::from(err).exception(&mut env, null_mut),
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_authenticate<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    token: JString<'local>,
) -> jboolean {
    let surreal = get_surreal_ref!(&mut env, ptr, || false as jboolean);
    let token_str = get_rust_string!(&mut env, token, || false as jboolean);
    if let Err(err) = TOKIO_RUNTIME.block_on(async {
        surreal.authenticate(token_str).await
    }) {
        return SurrealError::from(err).exception(&mut env, || false as jboolean);
    }
    true as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_invalidate<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    let surreal = get_surreal_ref!(&mut env, ptr, || false as jboolean);
    if let Err(err) = TOKIO_RUNTIME.block_on(async { surreal.invalidate().await }) {
        return SurrealError::from(err).exception(&mut env, || false as jboolean);
    }
    true as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_useNs<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    ns: JString<'local>,
) -> jobject {
    let surreal = get_surreal_ref!(&mut env, ptr, null_mut);
    let ns = get_rust_string!(&mut env, ns, null_mut);
    match TOKIO_RUNTIME.block_on(async { surreal.use_ns(ns).await }) {
        Ok((namespace, database)) => match new_ns_db_object(&mut env, namespace, database) {
            Ok(obj) => obj,
            Err(e) => SurrealError::from(e).exception(&mut env, null_mut),
        },
        Err(err) => SurrealError::from(err).exception(&mut env, null_mut),
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_useDb<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    db: JString<'local>,
) -> jobject {
    let surreal = get_surreal_ref!(&mut env, ptr, null_mut);
    let db = get_rust_string!(&mut env, db, null_mut);
    match TOKIO_RUNTIME.block_on(async { surreal.use_db(db).await }) {
        Ok((namespace, database)) => match new_ns_db_object(&mut env, namespace, database) {
            Ok(obj) => obj,
            Err(e) => SurrealError::from(e).exception(&mut env, null_mut),
        },
        Err(err) => SurrealError::from(err).exception(&mut env, null_mut),
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_useDefaults<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jobject {
    let surreal = get_surreal_ref!(&mut env, ptr, null_mut);
    match TOKIO_RUNTIME.block_on(async { surreal.use_defaults().await }) {
        Ok((namespace, database)) => match new_ns_db_object(&mut env, namespace, database) {
            Ok(obj) => obj,
            Err(e) => SurrealError::from(e).exception(&mut env, null_mut),
        },
        Err(err) => SurrealError::from(err).exception(&mut env, null_mut),
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_query<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    query: JString<'local>,
) -> jlong {
    // Retrieve the Surreal instance
    let surreal = get_surreal_ref!(&mut env, ptr, || 0);
    // Retrieve the query
    let query = get_rust_string!(&mut env, &query, || 0);
    // Execute the query
    let res = surrealdb_query::<()>(&surreal, &query, None);
    // Check the result
    let res = check_query_result!(&mut env, res, || 0);
    // Build a response instance
    JniTypes::new_response(Arc::new(Mutex::new(res)))
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_queryBind<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    query: JString<'local>,
    params_keys: JObjectArray<'local>,
    params_values: JLongArray<'local>,
) -> jlong {
    // Retrieve the Surreal instance
    let surreal = get_surreal_ref!(&mut env, ptr, || 0);
    // Retrieve the query
    let query = get_rust_string!(&mut env, &query, || 0);
    let keys = get_rust_string_array!(&mut env, params_keys, || 0);
    let value_ptrs = get_long_array!(&mut env, &params_values, || 0);
    let mut params_map = BTreeMap::<String, Value>::new();
    for (key, value_ptr) in keys.into_iter().zip(value_ptrs) {
        let value = get_value_mut_instance!(&mut env, value_ptr, || 0);
        params_map.insert(key, value.clone());
    }

    let res = surrealdb_query::<Value>(&surreal, &query, Some(params_map));
    let res = check_query_result!(&mut env, res, || 0);
    // Build a response instance
    JniTypes::new_response(Arc::new(Mutex::new(res)))
}

fn surrealdb_query<T>(
    surreal: &Surreal<Any>,
    query: &str,
    params: Option<BTreeMap<String, T>>,
) -> Result<IndexedResults>
where
    T: SurrealValue + Serialize + 'static,
{
    TOKIO_RUNTIME.block_on(async {
        let q = surreal.query(query);
        if let Some(p) = params {
            q.bind(p).await
        } else {
            q.await
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_createThingValue<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    thing_ptr: jlong,
    value_ptr: jlong,
) -> jlong {
    // Retrieve the Surreal instance
    let surreal = get_surreal_ref!(&mut env, surreal_ptr, || 0);
    // Extract the thing
    let thing = get_value_instance!(&mut env, thing_ptr, || 0);
    // Get the value
    let value = get_value_mut_instance!(&mut env, value_ptr, || 0);
    // Execute the query
    let query = format!("CREATE {} CONTENT $val", thing.to_sql());
    let params = BTreeMap::from([("val".to_string(), value.clone())]);
    let res = surrealdb_query(&surreal, &query, Some(params));
    // Check the result
    let mut response = check_query_result!(&mut env, res, || 0);
    // There is only one statement
    let mut result = take_one_result!(&mut env, response, || 0);
    // There should be only one result
    return_value_array_first!(result);
    // Otherwise we return an error
    return_unexpected_result!(&mut env, result.to_sql(), || 0)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_createTargetValues<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    target: JString<'local>,
    value_ptrs: JLongArray<'local>,
) -> jlongArray {
    // Retrieve the Surreal instance
    let surreal = get_surreal_ref!(&mut env, surreal_ptr, null_mut);
    // Build the parameters
    let target = get_rust_string!(&mut env, target, null_mut);
    // Get the pointers
    let value_ptrs = get_long_array!(&mut env, &value_ptrs, null_mut);
    // Build the queries
    let mut queries = Vec::with_capacity(value_ptrs.len());
    let mut params = BTreeMap::new();
    for (idx, value_ptr) in value_ptrs.iter().enumerate() {
        queries.push(format!("CREATE {} CONTENT $i{idx}", target));
        let value = get_value_mut_instance!(&mut env, *value_ptr, null_mut);
        params.insert(format!("i{idx}"), value.clone());
    }
    let query = queries.join(";\n");
    // Execute the query
    let res = surrealdb_query(&surreal, &query, Some(params));
    // Check the result
    let mut res = check_query_result!(&mut env, res, null_mut);
    // Prepare the result
    let mut value_ptrs: Vec<jlong> = Vec::with_capacity(res.num_statements());
    // Iterate over the statement
    for i in 0..res.num_statements() {
        let mut res = match res.take::<Value>(i) {
            Ok(r) => r,
            Err(e) => return SurrealError::SurrealDB(e).exception(&mut env, null_mut),
        };
        // There should be only one result per statement
        if let Value::Array(ref mut a) = res {
            if a.len() != 1 {
                return SurrealError::SurrealDBJni(format!("Unexpected result: {}", res.to_sql()))
                    .exception(&mut env, null_mut);
            }
            let val = a.remove(0);
            let value_ptr = JniTypes::new_value(val.into());
            value_ptrs.push(value_ptr);
        }
    }
    new_jlong_array!(&mut env, &value_ptrs, null_mut)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_insertTargetValues<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    target: JString<'local>,
    value_ptrs: JLongArray<'local>,
) -> jlongArray {
    // Retrieve the Surreal instance
    let surreal = get_surreal_ref!(&mut env, surreal_ptr, null_mut);
    // Build the parameters
    let target = get_rust_string!(&mut env, target, null_mut);
    // Get the pointers
    let value_ptrs = get_long_array!(&mut env, &value_ptrs, null_mut);
    // Build the queries
    let mut records = Vec::with_capacity(value_ptrs.len());
    for value_ptr in &value_ptrs {
        let value = get_value_mut_instance!(&mut env, *value_ptr, null_mut);
        records.push(value.to_sql());
    }
    let query = format!("INSERT INTO {} [ {} ]", target, records.join(" , "));
    // Execute the query
    let res = surrealdb_query::<()>(&surreal, &query, None);
    // Check the result
    let mut response = check_query_result!(&mut env, res, null_mut);
    // There is only one statement
    let result = take_one_result!(&mut env, response, null_mut);
    if let Value::Array(a) = result {
        // Prepare the result
        let mut value_ptrs: Vec<jlong> = Vec::with_capacity(a.len());
        for val in a.into_iter() {
            let value_ptr = JniTypes::new_value(val.into());
            value_ptrs.push(value_ptr);
        }
        new_jlong_array!(&mut env, &value_ptrs, null_mut)
    } else {
        SurrealError::SurrealDBJni(format!("Unexpected result: {}", result.to_sql()))
            .exception(&mut env, null_mut)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_insertRelationTargetValue<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    target: JString<'local>,
    value_ptr: jlong,
) -> jlong {
    // Retrieve the Surreal instance
    let surreal = get_surreal_ref!(&mut env, surreal_ptr, || 0);
    // Build the parameters
    let target = get_rust_string!(&mut env, target, || 0);
    // Get the value
    let value = get_value_mut_instance!(&mut env, value_ptr, || 0);
    // Execute the query
    let query = format!("INSERT RELATION INTO {} {}", target, value.to_sql());
    let res = surrealdb_query::<()>(&surreal, &query, None);
    // Check the result
    let mut response = check_query_result!(&mut env, res, || 0);
    // There is only one statement
    let mut result = take_one_result!(&mut env, response, || 0);
    // There should be only one result
    return_value_array_first!(result);
    // Otherwise we return an error
    return_unexpected_result!(&mut env, result.to_sql(), || 0)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_insertRelationTargetValues<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    target: JString<'local>,
    value_ptrs: JLongArray<'local>,
) -> jlongArray {
    // Retrieve the Surreal instance
    let surreal = get_surreal_ref!(&mut env, surreal_ptr, null_mut);
    // Build the parameters
    let target = get_rust_string!(&mut env, target, null_mut);
    // Get the pointers
    let value_ptrs = get_long_array!(&mut env, &value_ptrs, null_mut);
    // Build the queries
    let mut records = Vec::with_capacity(value_ptrs.len());
    for value_ptr in &value_ptrs {
        let value = get_value_mut_instance!(&mut env, *value_ptr, null_mut);
        records.push(value.to_sql());
    }
    let query = format!("INSERT RELATION INTO {} [ {} ]", target, records.join(" , "));
    // Execute the query
    let res = surrealdb_query::<()>(&surreal, &query, None);
    // Check the result
    let mut response = check_query_result!(&mut env, res, null_mut);
    // There is only one statement
    let result = take_one_result!(&mut env, response, null_mut);
    if let Value::Array(a) = result {
        // Prepare the result
        let mut value_ptrs: Vec<jlong> = Vec::with_capacity(a.len());
        for val in a.into_iter() {
            let value_ptr = JniTypes::new_value(val.into());
            value_ptrs.push(value_ptr);
        }
        new_jlong_array!(&mut env, &value_ptrs, null_mut)
    } else {
        SurrealError::SurrealDBJni(format!("Unexpected result: {}", result.to_sql()))
            .exception(&mut env, null_mut)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_relate<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    from_ptr: jlong,
    target: JString<'local>,
    to_ptr: jlong,
) -> jlong {
    // Retrieve the Surreal instance
    let surreal = get_surreal_ref!(&mut env, surreal_ptr, || 0);
    // Build the parameters
    let target = get_rust_string!(&mut env, target, || 0);
    // Get from and to
    let from_value = get_value_instance!(&mut env, from_ptr, || 0);
    let to_value = get_value_instance!(&mut env, to_ptr, || 0);
    // Execute the query
    let query = format!("RELATE $from->{}->$to", target);
    let params = BTreeMap::from([
        ("from".to_string(), from_value),
        ("to".to_string(), to_value),
    ]);
    let res = surrealdb_query(&surreal, &query, Some(params));
    // Check the result
    let mut response = check_query_result!(&mut env, res, || 0);
    // There is only one statement
    let mut result = take_one_result!(&mut env, response, || 0);
    // There should be only one result
    return_value_array_first!(result);
    // Otherwise we return an error
    return_unexpected_result!(&mut env, result.to_sql(), || 0)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_relateContent<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    from_ptr: jlong,
    target: JString<'local>,
    to_ptr: jlong,
    content_ptr: jlong,
) -> jlong {
    // Retrieve the Surreal instance
    let surreal = get_surreal_ref!(&mut env, surreal_ptr, || 0);
    // Build the parameters
    let target = get_rust_string!(&mut env, target, || 0);
    // Get from and to
    let from_value = get_value_instance!(&mut env, from_ptr, || 0);
    let to_value = get_value_instance!(&mut env, to_ptr, || 0);
    let content_value = get_value_mut_instance!(&mut env, content_ptr, || 0);
    // Execute the query
    let query = format!("RELATE $from->{}->$to CONTENT {}", target, content_value.to_sql());
    let params = BTreeMap::from([
        ("from".to_string(), from_value),
        ("to".to_string(), to_value),
    ]);
    let res = surrealdb_query(&surreal, &query, Some(params));
    // Check the result
    let mut response = check_query_result!(&mut env, res, || 0);
    // There is only one statement
    let mut result = take_one_result!(&mut env, response, || 0);
    // There should be only one result
    return_value_array_first!(result);
    // Otherwise we return an error
    return_unexpected_result!(&mut env, result.to_sql(), || 0)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_selectThing<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    thing_ptr: jlong,
) -> jlong {
    // Retrieve the Surreal instance
    let surreal = get_surreal_ref!(&mut env, surreal_ptr, || 0);
    // Extract the thing
    let thing = get_value_instance!(&mut env, thing_ptr, || 0);
    // Execute the query
    let query = format!("SELECT * FROM {}", thing.to_sql());
    let res = surrealdb_query::<()>(&surreal, &query, None);
    // Check the result
    let mut res = check_query_result!(&mut env, res, || 0);
    // There is only one statement
    let mut res = take_one_result!(&mut env, res, || 0);
    // There should be only one result
    return_value_array_first!(res);
    // If the array is empty, return null (0) for Optional.empty()
    if let Value::Array(ref a) = res {
        if a.is_empty() {
            return 0;
        }
    }
    // Otherwise throw an error
    return_unexpected_result!(&mut env, res.to_sql(), || 0)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_selectThings<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    thing_ptrs: JLongArray<'local>,
) -> jlongArray {
    // Retrieve the Surreal instance
    let surreal = get_surreal_ref!(&mut env, surreal_ptr, null_mut);
    // Get the thing pointers
    let thing_ptrs = get_long_array!(&mut env, &thing_ptrs, null_mut);
    // Extract the things
    let mut things = Vec::with_capacity(thing_ptrs.len());
    for thing_ptr in thing_ptrs {
        let thing = get_value_instance!(&mut env, thing_ptr, null_mut);
        things.push(thing.to_sql());
    }
    // Execute the query
    let query = format!("SELECT * FROM {}", things.join(","));
    let res = surrealdb_query::<()>(&surreal, &query, None);
    // Check the result
    let mut res = check_query_result!(&mut env, res, null_mut);
    // There is only one statement
    let res = take_one_result!(&mut env, res, null_mut);
    // Prepare the result
    if let Value::Array(a) = res {
        let mut value_ptrs: Vec<jlong> = Vec::with_capacity(a.len());
        for value in a {
            let value_ptr = JniTypes::new_value(Arc::new(value));
            value_ptrs.push(value_ptr);
        }
        // Return the results
        new_jlong_array!(&mut env, &value_ptrs, null_mut)
    } else {
        SurrealError::SurrealDBJni(format!("Unexpected result: {}", res.to_sql()))
            .exception(&mut env, null_mut)
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_selectTargetsValues<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    targets: JObjectArray<'local>,
) -> jlong {
    // Retrieve the Surreal instance
    let surreal = get_surreal_ref!(&mut env, surreal_ptr, || 0);
    // Get the targets
    let targets = get_rust_string_array!(&mut env, targets, || 0);
    // Prepare the query
    let query = format!("SELECT * FROM {}", targets.join(","));
    // Execute the query
    let res = surrealdb_query::<()>(&surreal, &query, None);
    // Check the result
    let mut response = check_query_result!(&mut env, res, || 0);
    // There is only one statement
    let result = take_one_result!(&mut env, response, || 0);
    // Return the iterator
    return_value_array_iter!(result);
    // Otherwise throw an error
    return_unexpected_result!(&mut env, result.to_sql(), || 0)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_selectTargetsValuesSync<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    targets: JObjectArray<'local>,
) -> jlong {
    // Retrieve the Surreal instance
    let surreal = get_surreal_ref!(&mut env, surreal_ptr, || 0);
    // Get the targets
    let targets = get_rust_string_array!(&mut env, targets, || 0);
    // Prepare the query
    let query = format!("SELECT * FROM {}", targets.join(","));
    // Execute the query
    let res = surrealdb_query::<()>(&surreal, &query, None);
    // Check the result
    let mut response = check_query_result!(&mut env, res, || 0);
    // There is only one statement
    let result = take_one_result!(&mut env, response, || 0);
    // Return tne sync iterator
    return_value_array_iter_sync!(result);
    // Otherwise throw an error
    return_unexpected_result!(&mut env, result.to_sql(), || 0)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_deleteThing<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    thing_ptr: jlong,
) -> jboolean {
    // Retrieve the Surreal instance
    let surreal = get_surreal_ref!(&mut env, surreal_ptr, || false as jboolean);
    // Build the parameters
    let thing = get_value_instance!(&mut env, thing_ptr, || false as jboolean);
    // Prepare the params
    let params = BTreeMap::from([("t".to_string(), thing)]);
    // Execute the query
    let res = surrealdb_query(&surreal, "DELETE $t", Some(params));
    // Check the result
    check_query_result!(&mut env, res, || false as jboolean);
    true as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_deleteThings<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    thing_ptrs: JLongArray<'local>,
) -> jboolean {
    // Retrieve the Surreal instance
    let surreal = get_surreal_ref!(&mut env, surreal_ptr, || false as jboolean);
    // Extract the things
    let thing_ptrs = get_long_array!(&mut env, &thing_ptrs, || false as jboolean);
    // Prepare the params
    let mut targets = Vec::with_capacity(thing_ptrs.len());
    let mut params = BTreeMap::new();
    for (idx, thing_ptr) in thing_ptrs.iter().enumerate() {
        let value = get_value_instance!(&mut env, *thing_ptr, || false as jboolean);
        params.insert(format!("t{idx}"), value);
        targets.push(format!("$t{idx}"));
    }
    // Prepare the query
    let query = format!("DELETE {}", targets.join(","));
    // Execute the query
    let res = surrealdb_query(&surreal, &query, Some(params));
    // Check the result
    check_query_result!(&mut env, res, || 0);
    true as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_deleteTarget<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    target: JString<'local>,
) -> jboolean {
    // Retrieve the Surreal instance
    let surreal = get_surreal_ref!(&mut env, surreal_ptr, || false as jboolean);
    // Get the targets
    let target = get_rust_string!(&mut env, target, || false as jboolean);
    // Prepare the query
    let query = format!("DELETE FROM {}", target);
    // Execute the query
    let res = surrealdb_query::<()>(&surreal, &query, None);
    // Check the result
    check_query_result!(&mut env, res, || false as jboolean);
    true as jboolean
}

fn up_thing_value(
    mut env: JNIEnv,
    surreal_ptr: jlong,
    thing_ptr: jlong,
    up_type: jint,
    value_ptr: jlong,
    up: &str,
) -> jlong {
    // Retrieve the Surreal instance
    let surreal = get_surreal_ref!(&mut env, surreal_ptr, || 0);
    // Extract the thing
    let thing = get_value_instance!(&mut env, thing_ptr, || 0);
    // Get the value
    let value = get_value_mut_instance!(&mut env, value_ptr, || 0);
    // Check the up type
    let up_type = convert_up_type!(&mut env, up_type, || 0);
    // Execute the query
    let query = format!("{up} {} {up_type} $val", thing.to_sql());
    let params = BTreeMap::from([("val".to_string(), value.clone())]);
    let res = surrealdb_query(&surreal, &query, Some(params));
    // Check the result
    let mut response = check_query_result!(&mut env, res, || 0);
    // There is only one statement
    let mut result: Value = take_one_result!(&mut env, response, || 0);
    // There should be only one result
    return_value_array_first!(result);
    // Otherwise we return an error
    return_unexpected_result!(&mut env, result.to_sql(), || 0)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_updateThingValue<'local>(
    env: JNIEnv<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    thing_ptr: jlong,
    up_type: jint,
    value_ptr: jlong,
) -> jlong {
    up_thing_value(env, surreal_ptr, thing_ptr, up_type, value_ptr, "update")
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_upsertThingValue<'local>(
    env: JNIEnv<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    thing_ptr: jlong,
    up_type: jint,
    value_ptr: jlong,
) -> jlong {
    up_thing_value(env, surreal_ptr, thing_ptr, up_type, value_ptr, "upsert")
}

fn up_target_value(
    mut env: JNIEnv,
    surreal_ptr: jlong,
    target: JString,
    up_type: jint,
    value_ptr: jlong,
    up: &str,
) -> jlong {
    // Retrieve the Surreal instance
    let surreal = get_surreal_ref!(&mut env, surreal_ptr, || 0);
    // Build the parameters
    let target = get_rust_string!(&mut env, target, || 0);
    // Get the value
    let value = get_value_mut_instance!(&mut env, value_ptr, || 0);
    // Check the up type
    let up_type = convert_up_type!(&mut env, up_type, || 0);
    // Execute the query
    let query = format!("{up} {} {up_type} $val", target);
    let params = BTreeMap::from([("val".to_string(), value.clone())]);
    let res = surrealdb_query(&surreal, &query, Some(params));
    // Check the result
    let mut response = check_query_result!(&mut env, res, || 0);
    // There is only one statement
    let result: Value = take_one_result!(&mut env, response, || 0);
    // There should be only one result
    return_value_array_iter!(result);
    // Otherwise we return an error
    return_unexpected_result!(&mut env, result.to_sql(), || 0)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_updateTargetValue<'local>(
    env: JNIEnv<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    target: JString<'local>,
    up_type: jint,
    value_ptr: jlong,
) -> jlong {
    up_target_value(env, surreal_ptr, target, up_type, value_ptr, "update")
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_upsertTargetValue<'local>(
    env: JNIEnv<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    target: JString<'local>,
    up_type: jint,
    value_ptr: jlong,
) -> jlong {
    up_target_value(env, surreal_ptr, target, up_type, value_ptr, "upsert")
}

fn up_target_value_sync<'local>(
    mut env: JNIEnv<'local>,
    surreal_ptr: jlong,
    target: JString<'local>,
    up_type: jint,
    value_ptr: jlong,
    up: &str,
) -> jlong {
    // Retrieve the Surreal instance
    let surreal = get_surreal_ref!(&mut env, surreal_ptr, || 0);
    // Build the parameters
    let target = get_rust_string!(&mut env, target, || 0);
    // Get the value
    let value = get_value_mut_instance!(&mut env, value_ptr, || 0);
    // Check the up type
    let up_type = convert_up_type!(&mut env, up_type, || 0);
    // Execute the query
    let query = format!("{up} {} {up_type} $val", target);
    let params = BTreeMap::from([("val".to_string(), value.clone())]);
    let res = surrealdb_query(&surreal, &query, Some(params));
    // Check the result
    let mut response = check_query_result!(&mut env, res, || 0);
    // There is only one statement
    let result: Value = take_one_result!(&mut env, response, || 0);
    // Return tne sync iterator
    return_value_array_iter_sync!(result);
    // Otherwise throw an error
    return_unexpected_result!(&mut env, result.to_sql(), || 0)
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_updateTargetValueSync<'local>(
    env: JNIEnv<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    target: JString<'local>,
    up_type: jint,
    value_ptr: jlong,
) -> jlong {
    up_target_value_sync(env, surreal_ptr, target, up_type, value_ptr, "update")
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_upsertTargetValueSync<'local>(
    env: JNIEnv<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    target: JString<'local>,
    up_type: jint,
    value_ptr: jlong,
) -> jlong {
    up_target_value_sync(env, surreal_ptr, target, up_type, value_ptr, "upsert")
}
