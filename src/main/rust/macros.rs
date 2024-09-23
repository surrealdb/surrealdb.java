#[macro_export]
macro_rules! get_rust_string {
    ($env:expr, $str:expr, $default_fn:expr) => {{
        match $env.get_string(&$str) {
            Ok(s) => String::from(s),
            Err(e) => return SurrealError::from(e).exception(&mut $env, $default_fn),
        }
    }};
}

#[macro_export]
macro_rules! get_surreal_instance {
    ($env:expr, $id:expr, $default_fn:expr) => {
        match $crate::get_instance::<Surreal<Any>>($id, "Surreal") {
            Ok(s) => s.clone(),
            Err(e) => return e.exception($env, $default_fn),
        }
    };
}

#[macro_export]
macro_rules! get_response_instance {
    ($env:expr, $id:expr, $default_fn:expr) => {
        match $crate::get_instance::<Arc<parking_lot::Mutex<Response>>>($id, "Response") {
            Ok(s) => s.clone(),
            Err(e) => return e.exception($env, $default_fn),
        }
    };
}

#[macro_export]
macro_rules! get_value_instance {
    ($env:expr, $id:expr, $default_fn:expr) => {
        match $crate::get_instance::<Arc<surrealdb::sql::Value>>($id, "Value") {
            Ok(s) => s.clone(),
            Err(e) => return e.exception($env, $default_fn),
        }
    };
}

#[macro_export]
macro_rules! get_entry_instance {
    ($env:expr, $id:expr, $default_fn:expr) => {
        match $crate::get_instance::<(String, Arc<surrealdb::sql::Value>)>($id, "Entry") {
            Ok(s) => s,
            Err(e) => return e.exception($env, $default_fn),
        }
    };
}

#[macro_export]
macro_rules! get_value_iterator_instance {
    ($env:expr, $id:expr, $default_fn:expr) => {
        match $crate::get_instance::<std::vec::IntoIter<surrealdb::sql::Value>>(
            $id,
            "ValueIterator",
        ) {
            Ok(s) => s,
            Err(e) => return e.exception($env, $default_fn),
        }
    };
}

#[macro_export]
macro_rules! get_entry_iterator_instance {
    ($env:expr, $id:expr, $default_fn:expr) => {
        match $crate::get_instance::<
            std::collections::btree_map::IntoIter<String, surrealdb::sql::Value>,
        >($id, "EntryIterator")
        {
            Ok(s) => s,
            Err(e) => return e.exception($env, $default_fn),
        }
    };
}

#[macro_export]
macro_rules! get_value_iterator_mut_instance {
    ($env:expr, $id:expr, $default_fn:expr) => {
        match $crate::get_instance_mut::<std::vec::IntoIter<surrealdb::sql::Value>>(
            $id,
            "ValueIterator",
        ) {
            Ok(s) => s,
            Err(e) => return e.exception($env, $default_fn),
        }
    };
}

#[macro_export]
macro_rules! get_entry_iterator_mut_instance {
    ($env:expr, $id:expr, $default_fn:expr) => {
        match $crate::get_instance_mut::<
            std::collections::btree_map::IntoIter<String, surrealdb::sql::Value>,
        >($id, "EntryIterator")
        {
            Ok(s) => s,
            Err(e) => return e.exception($env, $default_fn),
        }
    };
}

#[macro_export]
macro_rules! get_sync_value_iterator_instance {
    ($env:expr, $id:expr, $default_fn:expr) => {
        match $crate::get_instance::<
            Arc<parking_lot::Mutex<std::vec::IntoIter<surrealdb::sql::Value>>>,
        >($id, "SynchronizedValueIterator")
        {
            Ok(s) => s.clone(),
            Err(e) => return e.exception($env, $default_fn),
        }
    };
}

#[macro_export]
macro_rules! get_sync_entry_iterator_instance {
    ($env:expr, $id:expr, $default_fn:expr) => {
        match $crate::get_instance::<
            Arc<
                parking_lot::Mutex<
                    std::collections::btree_map::IntoIter<String, surrealdb::sql::Value>,
                >,
            >,
        >($id, "SynchronizedValueIterator")
        {
            Ok(s) => s.clone(),
            Err(e) => return e.exception($env, $default_fn),
        }
    };
}

#[macro_export]
macro_rules! new_string {
    ($env:expr, $str:expr, $default_fn:expr) => {
        match $env.new_string($str) {
            Ok(output) => output.into_raw(),
            Err(e) => return $crate::SurrealError::from(e).exception($env, $default_fn),
        }
    };
}

#[macro_export]
macro_rules! get_long_array {
    ($env:expr, $ptrs:expr, $default_fn:expr) => {{
        let length = match $env.get_array_length($ptrs) {
            Ok(l) => l,
            Err(e) => return $crate::SurrealError::from(e).exception($env, $default_fn),
        };
        let mut long_ptrs: Vec<jlong> = vec![0; length as usize];
        if let Err(e) = $env.get_long_array_region($ptrs, 0, &mut long_ptrs) {
            return $crate::SurrealError::from(e).exception($env, $default_fn);
        };
        long_ptrs
    }};
}

#[macro_export]
macro_rules! new_jlong_array {
    ($env:expr, $array:expr, $default_fn:expr) => {{
        // Create a new jlongArray with the appropriate length
        let mut jarray = match $env.new_long_array($array.len() as jni::sys::jsize) {
            Ok(a) => a,
            Err(e) => return $crate::SurrealError::from(e).exception($env, $default_fn),
        };
        // Set the values of the jlongArray
        $env.set_long_array_region(&mut jarray, 0, $array).unwrap();
        // Return the populated jlongArray
        jarray.into_raw()
    }};
}

#[macro_export]
macro_rules! new_double_point {
    ($env:expr, $pt:expr, $default_fn:expr) => {{
        let double_array = match $env.new_double_array(2) {
            Ok(d) => d,
            Err(e) => return $crate::SurrealError::from(e).exception($env, $default_fn),
        };
        let coordinates: [jni::sys::jdouble; 2] = [$pt.x(), $pt.y()];
        if let Err(e) = $env.set_double_array_region(&double_array, 0, &coordinates) {
            return $crate::SurrealError::from(e).exception($env, $default_fn);
        }
        double_array.into_raw()
    }};
}

#[macro_export]
macro_rules! take_value_mut_instance {
    ($env:expr, $id:expr, $default_fn:expr) => {
        match $crate::take_instance::<surrealdb::sql::Value>($id, "ValueMut") {
            Ok(s) => s,
            Err(e) => return e.exception($env, $default_fn),
        }
    };
}

#[macro_export]
macro_rules! get_value_mut_instance {
    ($env:expr, $id:expr, $default_fn:expr) => {
        match $crate::get_instance::<surrealdb::sql::Value>($id, "ValueMut") {
            Ok(s) => s,
            Err(e) => return e.exception($env, $default_fn),
        }
    };
}

#[macro_export]
macro_rules! take_entry_mut_instance {
    ($env:expr, $id:expr, $default_fn:expr) => {
        match $crate::take_instance::<(String, surrealdb::sql::Value)>($id, "EntryMut") {
            Ok(s) => s,
            Err(e) => return e.exception($env, $default_fn),
        }
    };
}

#[macro_export]
macro_rules! get_entry_mut_instance {
    ($env:expr, $id:expr, $default_fn:expr) => {
        match $crate::get_instance::<(String, surrealdb::sql::Value)>($id, "EntryMut") {
            Ok(s) => s,
            Err(e) => return e.exception($env, $default_fn),
        }
    };
}

#[macro_export]
macro_rules! take_one_result {
    ($env:expr, $res:expr, $default_fn:expr) => {
        match $res.take::<surrealdb::Value>(0) {
            Ok(r) => {
                let r: surrealdb::sql::Value = r.into_inner();
                r
            },
            Err(e) => return $crate::SurrealError::SurrealDB(e).exception($env, $default_fn),
        }
    };
}

#[macro_export]
macro_rules! return_value_array_first {
    ($val:expr) => {
        if let surrealdb::sql::Value::Array(ref mut a) = $val {
            if a.len() == 1 {
                return $crate::create_instance(Arc::new(a.remove(0)));
            }
        }
    };
}

#[macro_export]
macro_rules! return_value_array_iter {
    ($val:expr) => {
        if let surrealdb::sql::Value::Array(a) = $val {
            let iter = a.into_iter();
            return $crate::create_instance(iter);
        }
    };
}

#[macro_export]
macro_rules! return_value_array_iter_sync {
    ($val:expr) => {
        if let surrealdb::sql::Value::Array(a) = $val {
            let iter = a.into_iter();
            return $crate::create_instance(std::sync::Arc::new(parking_lot::Mutex::new(iter)));
        }
    };
}

#[macro_export]
macro_rules! check_query_result {
    ($env:expr, $res:expr, $default_fn:expr) => {
        match $res {
            Ok(res) => res,
            Err(e) => {
                return $crate::SurrealError::SurrealDB(e).exception($env, $default_fn);
            }
        }
    };
}

#[macro_export]
macro_rules! parse_value {
    ($env:expr, $val:expr, $default_fn:expr) => {
        match surrealdb::sql::value($val) {
            Ok(v) => v,
            Err(e) => {
                return $crate::SurrealError::SurrealDBJni(e.to_string())
                    .exception($env, $default_fn)
            }
        }
    };
}

#[macro_export]
macro_rules! return_unexpected_result {
    ($env:expr, $res:expr, $default_fn:expr) => {
        return $crate::SurrealError::SurrealDBJni(format!("Unexpected result: {}", $res))
            .exception($env, $default_fn)
    };
}
