#[macro_export]
macro_rules! get_rust_string {
    ($env:expr, $str:expr, $default_fn:expr) => {
        match $env.get_string(&$str) {
            Ok(s) => String::from(s),
            Err(e) => return SurrealError::from(e).exception(&mut $env, $default_fn),
        }
    };
}

#[macro_export]
macro_rules! get_surreal_instance {
    ($env:expr, $id:expr, $default_fn:expr) => {
        match crate::get_instance::<Surreal<Any>>($id, "Surreal") {
            Ok(s) => s.clone(),
            Err(e) => return e.exception($env, $default_fn),
        }
    };
}

#[macro_export]
macro_rules! get_response_instance {
    ($env:expr, $id:expr, $default_fn:expr) => {
        match crate::get_instance::<Arc<parking_lot::Mutex<Response>>>($id, "Response") {
            Ok(s) => s.clone(),
            Err(e) => return e.exception($env, $default_fn),
        }
    };
}

#[macro_export]
macro_rules! get_value_instance {
    ($env:expr, $id:expr, $default_fn:expr) => {
        match crate::get_instance::<Arc<Value>>($id, "Value") {
            Ok(s) => s.clone(),
            Err(e) => return e.exception($env, $default_fn),
        }
    };
}

#[macro_export]
macro_rules! new_string {
    ($env:expr, $str:expr, $default_fn:expr) => {
        match $env.new_string($str) {
            Ok(output) => return output.into_raw(),
            Err(e) => return crate::SurrealError::from(e).exception($env, $default_fn),
        }
    };
}