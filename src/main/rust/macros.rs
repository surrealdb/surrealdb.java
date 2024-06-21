#[macro_export]
macro_rules! get_rust_string {
    ($env:expr, $str:expr, $default_fn:expr) => {
        match $env.get_string(&$str) {
            Ok(s) => String::from(s),
            Err(e) => return DriverError::from(e).exception(&mut $env, $default_fn),
        }
    };
}

#[macro_export]
macro_rules! get_surreal_instance {
    ($env:expr, $id:expr, $default_fn:expr) => {
        match crate::get_arc_instance::<Surreal<Any>>($id, "Surreal") {
            Ok(s) => s,
            Err(e) => return e.exception(&mut $env, $default_fn),
        }
    };
}