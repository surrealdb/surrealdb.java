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
        match crate::get_instance::<Arc<surrealdb::sql::Value>>($id, "Value") {
            Ok(s) => s.clone(),
            Err(e) => return e.exception($env, $default_fn),
        }
    };
}

#[macro_export]
macro_rules! get_entry_instance {
    ($env:expr, $id:expr, $default_fn:expr) => {
        match crate::get_instance::<(String, Arc<surrealdb::sql::Value>)>($id, "Entry") {
            Ok(s) => s,
            Err(e) => return e.exception($env, $default_fn),
        }
    };
}

#[macro_export]
macro_rules! get_value_iterator_instance {
    ($env:expr, $id:expr, $default_fn:expr) => {
        match crate::get_instance::<std::vec::IntoIter<surrealdb::sql::Value>>($id, "ValueIterator") {
            Ok(s) => s,
            Err(e) => return e.exception($env, $default_fn),
        }
    };
}

#[macro_export]
macro_rules! get_entry_iterator_instance {
    ($env:expr, $id:expr, $default_fn:expr) => {
        match crate::get_instance::<std::collections::btree_map::IntoIter<String, surrealdb::sql::Value>>($id, "EntryIterator") {
            Ok(s) => s,
            Err(e) => return e.exception($env, $default_fn),
        }
    };
}

#[macro_export]
macro_rules! get_value_iterator_mut_instance {
    ($env:expr, $id:expr, $default_fn:expr) => {
        match crate::get_instance_mut::<std::vec::IntoIter<surrealdb::sql::Value>>($id, "ValueIterator") {
            Ok(s) => s,
            Err(e) => return e.exception($env, $default_fn),
        }
    };
}

#[macro_export]
macro_rules! get_entry_iterator_mut_instance {
    ($env:expr, $id:expr, $default_fn:expr) => {
        match crate::get_instance_mut::<std::collections::btree_map::IntoIter<String, surrealdb::sql::Value>>($id, "EntryIterator") {
            Ok(s) => s,
            Err(e) => return e.exception($env, $default_fn),
        }
    };
}

#[macro_export]
macro_rules! get_sync_value_iterator_instance {
    ($env:expr, $id:expr, $default_fn:expr) => {
        match crate::get_instance::<Arc<parking_lot::Mutex<std::vec::IntoIter<surrealdb::sql::Value>>>>($id, "SynchronizedValueIterator") {
            Ok(s) => s.clone(),
            Err(e) => return e.exception($env, $default_fn),
        }
    };
}

#[macro_export]
macro_rules! get_sync_entry_iterator_instance {
    ($env:expr, $id:expr, $default_fn:expr) => {
        match crate::get_instance::<Arc<parking_lot::Mutex<std::collections::btree_map::IntoIter<String, surrealdb::sql::Value>>>>($id, "SynchronizedValueIterator") {
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

#[macro_export]
macro_rules! new_double_point {
    ($env:expr, $pt:expr, $default_fn:expr) => {
        {
            let double_array = match $env.new_double_array(2) {
                Ok(d) => d,
                Err(e) => return crate::SurrealError::from(e).exception($env, $default_fn)
            };
            let coordinates: [jni::sys::jdouble; 2] = [$pt.x(), $pt.y()];
            if let Err(e) = $env.set_double_array_region(&double_array, 0, &coordinates) {
                return crate::SurrealError::from(e).exception($env, $default_fn);
            }
            double_array.into_raw()
        }
    };
}