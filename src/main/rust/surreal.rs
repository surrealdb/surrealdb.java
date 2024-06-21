use std::collections::BTreeMap;
use std::ptr::null_mut;
use std::sync::Arc;

use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::{jboolean, jlong, jstring};
use parking_lot::Mutex;
use surrealdb::engine::any::Any;
use surrealdb::opt::auth::Root;
use surrealdb::sql::Value;
use surrealdb::Surreal;

use crate::{create_instance, get_rust_string, get_surreal_instance, new_string, release_instance, TOKIO_RUNTIME};
use crate::error::SurrealError;

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_newInstance<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
) -> jlong {
    create_instance::<Surreal<Any>>(Surreal::init())
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_deleteInstance<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
    id: jlong,
) -> jboolean {
    release_instance::<Surreal<Any>>(id);
    true as jboolean
}


#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_connect<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    id: jlong,
    addr: JString<'local>,
) -> jboolean {
    let surreal = get_surreal_instance!(&mut env, id, ||false as jboolean);
    let addr = get_rust_string!(env, addr, ||false as jboolean);
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
    id: jlong,
    username: JString<'local>,
    password: JString<'local>,
) -> jstring {
    // Retrieve the Surreal instance
    let surreal = get_surreal_instance!(&mut env, id, ||null_mut());
    // Convert the parameters
    let username = get_rust_string!(&mut env, username, ||null_mut());
    let password = get_rust_string!(&mut env, password, ||null_mut());
    // Signin
    match TOKIO_RUNTIME.block_on(async {
        surreal.signin(Root { username: &username, password: &password }).await
    }) {
        Ok(jwt) => {
            let jwt = jwt.into_insecure_token();
            new_string!(&mut env, jwt, ||null_mut())
        }
        Err(err) => SurrealError::from(err).exception(&mut env, || null_mut()),
    }
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_useNs<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    id: jlong,
    ns: JString<'local>,
) -> jboolean {
    // Retrieve the Surreal instance
    let surreal = get_surreal_instance!(&mut env, id, ||false as jboolean);
    // Convert the parameters
    let ns = get_rust_string!(&mut env, ns, ||false as jboolean);
    // Call use_ns
    if let Err(err) = TOKIO_RUNTIME.block_on(async {
        surreal.use_ns(ns).await
    }) {
        return SurrealError::from(err).exception(&mut env, || false as jboolean);
    }
    true.into()
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_useDb<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    id: jlong,
    db: JString<'local>,
) -> jboolean {
    // Retrieve the Surreal instance
    let surreal = get_surreal_instance!(&mut env, id, ||false as jboolean);
    // Call use_db
    let db: String = match env.get_string(&db) {
        Ok(s) => s.into(),
        Err(_) => return 0,
    };
    if let Err(err) = TOKIO_RUNTIME.block_on(async {
        surreal.use_db(db).await
    }) {
        return SurrealError::from(err).exception(&mut env, || false as jboolean);
    }
    true.into()
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_query<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    id: jlong,
    query: JString<'local>,
) -> jlong {
    surrealdb_query(&mut env, id, query, None)
}

fn surrealdb_query<'local>(
    env: &mut JNIEnv<'local>,
    id: jlong,
    query: JString<'local>,
    params: Option<BTreeMap<String, Value>>,
) -> jlong {
    // Retrieve the Surreal instance
    let surreal = get_surreal_instance!(env, id, ||0);
    let query: String = match env.get_string(&query) {
        Ok(s) => s.into(),
        Err(_) => return 0,
    };

    // Execute the query
    let response = match TOKIO_RUNTIME.block_on(async {
        let q = surreal.query(query);
        if let Some(p) = params {
            q.bind(p).await
        } else {
            q.await
        }
    }) {
        Ok(r) => r,
        Err(e) => return SurrealError::from(e).exception(env, || 0)
    };
    // Build a response instance
    create_instance(Arc::new(Mutex::new(response)))
}
