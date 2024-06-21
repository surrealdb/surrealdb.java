use std::ptr::null_mut;

use jni::JNIEnv;
use jni::objects::{JClass, JString};
use jni::sys::{jboolean, jlong, jstring};
use surrealdb::engine::any::Any;
use surrealdb::opt::auth::Root;
use surrealdb::Surreal;

use crate::{create_arc_instance, get_rust_string, get_surreal_instance, release_arc_instance, TOKIO_RUNTIME};
use crate::error::DriverError;

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_new_1instance<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
) -> jlong {
    create_arc_instance::<Surreal<Any>>(Surreal::init())
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_delete_1instance<'local>(
    _env: JNIEnv<'local>,
    _class: JClass<'local>,
    id: jlong,
) -> jboolean {
    release_arc_instance::<Surreal<Any>>(id);
    true as jboolean
}


#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Surreal_connect<'local>(
    mut env: JNIEnv<'local>,
    _class: JClass<'local>,
    id: jlong,
    addr: JString<'local>,
) -> jboolean {
    let surreal = get_surreal_instance!(env, id, ||false as jboolean);
    let addr = get_rust_string!(env, addr, ||false as jboolean);
    // Connect
    if let Err(err) = TOKIO_RUNTIME.block_on(async { surreal.connect(addr).await }) {
        return DriverError::from(err).exception(&mut env, || false as jboolean);
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
    let surreal = get_surreal_instance!(env, id, ||null_mut());
    // Convert the parameters
    let username = get_rust_string!(env, username, ||null_mut());
    let password = get_rust_string!(env, password, ||null_mut());
    // Signin
    match TOKIO_RUNTIME.block_on(async {
        surreal.signin(Root { username: &username, password: &password }).await
    }) {
        Ok(jwt) => {
            let jwt = jwt.into_insecure_token();
            match env.new_string(jwt) {
                Ok(output) => return output.into_raw(),
                Err(e) => DriverError::from(e).exception(&mut env, || null_mut()),
            }
        }
        Err(err) => DriverError::from(err).exception(&mut env, || null_mut()),
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
    let surreal = get_surreal_instance!(env, id, ||false as jboolean);
    // Convert the parameters
    let ns = get_rust_string!(env, ns, ||false as jboolean);
    // Call use_ns
    if let Err(err) = TOKIO_RUNTIME.block_on(async {
        surreal.use_ns(ns).await
    }) {
        return DriverError::from(err).exception(&mut env, || false as jboolean);
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
    let surreal = get_surreal_instance!(env, id, ||false as jboolean);
    // Call use_db
    let db: String = match env.get_string(&db) {
        Ok(s) => s.into(),
        Err(_) => return 0,
    };
    if let Err(err) = TOKIO_RUNTIME.block_on(async {
        surreal.use_db(db).await
    }) {
        return DriverError::from(err).exception(&mut env, || false as jboolean);
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
    // Retrieve the Surreal instance
    let surreal = get_surreal_instance!(env, id, ||0);
    let query: String = match env.get_string(&query) {
        Ok(s) => s.into(),
        Err(_) => return 0,
    };
    let response = match TOKIO_RUNTIME.block_on(async {
        surreal.query(query).await
    }) {
        Ok(r) => r,
        Err(e) => return DriverError::from(e).exception(&mut env, || 0)
    };
    let response = Box::new(response);
    Box::into_raw(response) as jlong
}