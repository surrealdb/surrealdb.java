use std::collections::BTreeMap;
use std::ptr::null_mut;
use std::sync::Arc;

use crate::error::SurrealError;
use crate::{
    check_query_result, check_value_table, convert_up_type, get_long_array, get_rust_string,
    get_rust_string_array, get_surreal_instance, get_value_instance, get_value_mut_instance,
    new_jlong_array, new_string, parse_value, release_instance, return_unexpected_result,
    return_value_array_first, return_value_array_iter, return_value_array_iter_sync,
    take_one_result, JniTypes, TOKIO_RUNTIME,
};
use jni::objects::{JClass, JLongArray, JObjectArray, JString};
use jni::sys::{jboolean, jint, jlong, jlongArray, jstring};
use jni::JNIEnv;
use parking_lot::Mutex;
use serde::Serialize;
use surrealdb::engine::any::Any;
use surrealdb::opt::auth::{Database, Namespace, Root};
use surrealdb::types::{SurrealValue, ToSql, Value};
use surrealdb::{IndexedResults, Result, Surreal};

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
) -> jboolean {
    release_instance::<Surreal<Any>>(ptr);
    true as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_connect<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    addr: JString<'local>,
) -> jboolean {
    let surreal = get_surreal_instance!(&mut env, ptr, || false as jboolean);
    let addr = get_rust_string!(env, addr, || false as jboolean);
    // Connect
    if let Err(err) = TOKIO_RUNTIME.block_on(async { surreal.connect(addr).await }) {
        return SurrealError::from(err).exception(&mut env, || false as jboolean);
    }
    true as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_signinRoot<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    username: JString<'local>,
    password: JString<'local>,
) -> jstring {
    // Retrieve the Surreal instance
    let surreal = get_surreal_instance!(&mut env, ptr, null_mut);
    // Convert the parameters
    let username = get_rust_string!(&mut env, username, null_mut);
    let password = get_rust_string!(&mut env, password, null_mut);
    // Signin
    match TOKIO_RUNTIME.block_on(async {
        surreal
            .signin(Root {
                username,
                password,
            })
            .await
    }) {
        Ok(jwt) => {
            let jwt = jwt.into_insecure_token();
            new_string!(&mut env, jwt, null_mut)
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
) -> jstring {
    // Retrieve the Surreal instance
    let surreal = get_surreal_instance!(&mut env, ptr, null_mut);
    // Convert the parameters
    let username = get_rust_string!(&mut env, username, null_mut);
    let password = get_rust_string!(&mut env, password, null_mut);
    let namespace = get_rust_string!(&mut env, ns, null_mut);
    // Signin
    match TOKIO_RUNTIME.block_on(async {
        surreal
            .signin(Namespace {
                username,
                password,
                namespace,
            })
            .await
    }) {
        Ok(jwt) => {
            let jwt = jwt.into_insecure_token();
            new_string!(&mut env, jwt, null_mut)
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
) -> jstring {
    // Retrieve the Surreal instance
    let surreal = get_surreal_instance!(&mut env, ptr, null_mut);
    // Convert the parameters
    let username = get_rust_string!(&mut env, username, null_mut);
    let password = get_rust_string!(&mut env, password, null_mut);
    let namespace = get_rust_string!(&mut env, ns, null_mut);
    let database = get_rust_string!(&mut env, db, null_mut);
    // Signin
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
        Ok(jwt) => {
            let jwt = jwt.into_insecure_token();
            new_string!(&mut env, jwt, null_mut)
        }
        Err(err) => SurrealError::from(err).exception(&mut env, null_mut),
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_useNs<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    ns: JString<'local>,
) -> jboolean {
    // Retrieve the Surreal instance
    let surreal = get_surreal_instance!(&mut env, ptr, || false as jboolean);
    // Convert the parameters
    let ns = get_rust_string!(&mut env, ns, || false as jboolean);
    // Call use_ns
    if let Err(err) = TOKIO_RUNTIME.block_on(async { surreal.use_ns(ns).await }) {
        return SurrealError::from(err).exception(&mut env, || false as jboolean);
    }
    true.into()
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_useDb<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    db: JString<'local>,
) -> jboolean {
    // Retrieve the Surreal instance
    let surreal = get_surreal_instance!(&mut env, ptr, || false as jboolean);
    // Call use_db
    let db: String = match env.get_string(&db) {
        Ok(s) => s.into(),
        Err(_) => return 0,
    };
    if let Err(err) = TOKIO_RUNTIME.block_on(async { surreal.use_db(db).await }) {
        return SurrealError::from(err).exception(&mut env, || false as jboolean);
    }
    true.into()
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_query<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    ptr: jlong,
    query: JString<'local>,
) -> jlong {
    // Retrieve the Surreal instance
    let surreal = get_surreal_instance!(&mut env, ptr, || 0);
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
    let surreal = get_surreal_instance!(&mut env, ptr, || 0);
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
    let surreal = get_surreal_instance!(&mut env, surreal_ptr, || 0);
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
    let surreal = get_surreal_instance!(&mut env, surreal_ptr, null_mut);
    // Build the parameters
    let target = get_rust_string!(&mut env, target, null_mut);
    // Parse the target
    let target = parse_value!(&mut env, &target, null_mut);
    // Check the target is a table
    let table = check_value_table!(&mut env, target, null_mut);
    // Get the pointers
    let value_ptrs = get_long_array!(&mut env, &value_ptrs, null_mut);
    // Build the queries
    let mut queries = Vec::with_capacity(value_ptrs.len());
    let mut params = BTreeMap::new();
    for (idx, value_ptr) in value_ptrs.iter().enumerate() {
        queries.push(format!("CREATE {} CONTENT $i{idx}", table.to_sql()));
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
    let surreal = get_surreal_instance!(&mut env, surreal_ptr, null_mut);
    // Build the parameters
    let target = get_rust_string!(&mut env, target, null_mut);
    // Parse the target
    let target = parse_value!(&mut env, &target, null_mut);
    // Check the target is a table
    let table = check_value_table!(&mut env, target, null_mut);
    // Get the pointers
    let value_ptrs = get_long_array!(&mut env, &value_ptrs, null_mut);
    // Build the queries
    let mut records = Vec::with_capacity(value_ptrs.len());
    for value_ptr in &value_ptrs {
        let value = get_value_mut_instance!(&mut env, *value_ptr, null_mut);
        records.push(value.to_sql());
    }
    let query = format!("INSERT INTO {} [ {} ]", table.to_sql(), records.join(" , "));
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
    let surreal = get_surreal_instance!(&mut env, surreal_ptr, || 0);
    // Build the parameters
    let target = get_rust_string!(&mut env, target, || 0);
    // Parse the target
    let target = parse_value!(&mut env, &target, || 0);
    let table = check_value_table!(&mut env, target, || 0);
    // Get the value
    let value = get_value_mut_instance!(&mut env, value_ptr, || 0);
    // Execute the query
    let query = format!("INSERT RELATION INTO {} $val", table.to_sql());
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
pub extern "system" fn Java_com_surrealdb_Surreal_insertRelationTargetValues<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    target: JString<'local>,
    value_ptrs: JLongArray<'local>,
) -> jlongArray {
    // Retrieve the Surreal instance
    let surreal = get_surreal_instance!(&mut env, surreal_ptr, null_mut);
    // Build the parameters
    let target = get_rust_string!(&mut env, target, null_mut);
    // Parse the target
    let target = parse_value!(&mut env, &target, null_mut);
    let table = check_value_table!(&mut env, target, null_mut);
    // Get the pointers
    let value_ptrs = get_long_array!(&mut env, &value_ptrs, null_mut);
    // Build the queries
    let mut records = Vec::with_capacity(value_ptrs.len());
    for value_ptr in &value_ptrs {
        let value = get_value_mut_instance!(&mut env, *value_ptr, null_mut);
        records.push(value.to_sql());
    }
    let query = format!("INSERT RELATION INTO {} [ {} ]", table.to_sql(), records.join(" , "));
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
    let surreal = get_surreal_instance!(&mut env, surreal_ptr, || 0);
    // Build the parameters
    let target = get_rust_string!(&mut env, target, || 0);
    // Parse the target
    let target = parse_value!(&mut env, &target, || 0);
    let table = check_value_table!(&mut env, target, || 0);
    // Get from and to
    let from_value = get_value_instance!(&mut env, from_ptr, || 0);
    let to_value = get_value_instance!(&mut env, to_ptr, || 0);
    // Execute the query
    let query = format!("RELATE $from->{}->$to", table.to_sql());
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
    let surreal = get_surreal_instance!(&mut env, surreal_ptr, || 0);
    // Build the parameters
    let target = get_rust_string!(&mut env, target, || 0);
    // Parse the target
    let target = parse_value!(&mut env, &target, || 0);
    let table = check_value_table!(&mut env, target, || 0);
    // Get from and to
    let from_value = get_value_instance!(&mut env, from_ptr, || 0);
    let to_value = get_value_instance!(&mut env, to_ptr, || 0);
    let content_value = get_value_mut_instance!(&mut env, content_ptr, || 0);
    // Execute the query
    let query = format!("RELATE $from->{}->$to CONTENT {}", table.to_sql(), content_value.to_sql());
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
    let surreal = get_surreal_instance!(&mut env, surreal_ptr, || 0);
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
    0
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_selectThings<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    surreal_ptr: jlong,
    thing_ptrs: JLongArray<'local>,
) -> jlongArray {
    // Retrieve the Surreal instance
    let surreal = get_surreal_instance!(&mut env, surreal_ptr, null_mut);
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
    let surreal = get_surreal_instance!(&mut env, surreal_ptr, || 0);
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
    let surreal = get_surreal_instance!(&mut env, surreal_ptr, || 0);
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
    let surreal = get_surreal_instance!(&mut env, surreal_ptr, || false as jboolean);
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
    let surreal = get_surreal_instance!(&mut env, surreal_ptr, || false as jboolean);
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
    let surreal = get_surreal_instance!(&mut env, surreal_ptr, || false as jboolean);
    // Get the targets
    let target = get_rust_string!(&mut env, target, || false as jboolean);
    // Parse the targets
    let target = parse_value!(&mut env, &target, || false as jboolean);
    // Check the target is a table
    let table = check_value_table!(&mut env, &target, || false as jboolean);
    // Prepare the query
    let query = format!("DELETE FROM {}", table.to_sql());
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
    let surreal = get_surreal_instance!(&mut env, surreal_ptr, || 0);
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
    let surreal = get_surreal_instance!(&mut env, surreal_ptr, || 0);
    // Build the parameters
    let target = get_rust_string!(&mut env, target, || 0);
    // Parse the targets
    let target = parse_value!(&mut env, &target, || 0);
    // Check the value is a table
    let table = check_value_table!(&mut env, target, || 0);
    // Get the value
    let value = get_value_mut_instance!(&mut env, value_ptr, || 0);
    // Check the up type
    let up_type = convert_up_type!(&mut env, up_type, || 0);
    // Execute the query
    let query = format!("{up} {} {up_type} $val", table.to_sql());
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
    let surreal = get_surreal_instance!(&mut env, surreal_ptr, || 0);
    // Build the parameters
    let target = get_rust_string!(&mut env, target, || 0);
    // Parse the target
    let target = parse_value!(&mut env, &target, || 0);
    // Check the value is a table
    let table = check_value_table!(&mut env, target, || 0);
    // Get the value
    let value = get_value_mut_instance!(&mut env, value_ptr, || 0);
    // Check the up type
    let up_type = convert_up_type!(&mut env, up_type, || 0);
    // Execute the query
    let query = format!("{up} {} {up_type} $val", table.to_sql());
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
