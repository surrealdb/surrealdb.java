use std::hash::{DefaultHasher, Hash, Hasher};
use std::ptr::null_mut;
use std::sync::Arc;

use crate::with_env_body;
use geo::{Coord, LineString, MultiLineString, MultiPoint, MultiPolygon, Point, Polygon};
use jni::objects::{JClass, JDoubleArray, JLongArray, JObjectArray};
use jni::sys::{jboolean, jdouble, jdoubleArray, jint, jlong, jlongArray, jstring};
use jni::{Env, EnvUnowned};
use surrealdb::types::{Geometry, Value};

use crate::error::SurrealError;
use crate::{
    get_long_array, get_value_instance, new_double_point, new_jlong_array, new_string,
    release_instance, JniTypes,
};

/// Unwraps a `Result` inside a `jlong`-returning native body, surfacing any error
/// (either a [`SurrealError`] or a `jni::errors::Error`) as a Java exception and
/// returning `0`. `SurrealError::from` is an identity conversion for `SurrealError`
/// and converts JNI errors, so this handles both error kinds.
macro_rules! unwrap_or_throw {
    ($env:expr, $result:expr) => {
        match $result {
            Ok(v) => v,
            Err(e) => return SurrealError::from(e).exception($env, || 0),
        }
    };
}

/// Reads a flat `double[]` of `[x0, y0, x1, y1, ...]` into a `Vec<Coord<f64>>`.
fn read_coords(env: &mut Env, arr: JDoubleArray) -> Result<Vec<Coord<f64>>, SurrealError> {
    let len = arr.len(env)?;
    let mut buf = vec![0.0_f64; len];
    arr.get_region(env, 0, &mut buf)?;
    let mut coords = Vec::with_capacity(len / 2);
    let mut i = 0;
    while i + 1 < len {
        coords.push(Coord {
            x: buf[i],
            y: buf[i + 1],
        });
        i += 2;
    }
    Ok(coords)
}

/// Reads a `double[][]` (each inner array a flat coordinate list) into a
/// `Vec<LineString<f64>>` — used for polygon rings and multi-line components.
fn read_rings(
    env: &mut Env,
    arr: JObjectArray<JDoubleArray>,
) -> Result<Vec<LineString<f64>>, SurrealError> {
    let len = arr.len(env)?;
    let mut rings = Vec::with_capacity(len);
    for i in 0..len {
        let inner = arr.get_element(env, i)?;
        rings.push(LineString::new(read_coords(env, inner)?));
    }
    Ok(rings)
}

/// Builds a polygon from its rings: the first ring is the exterior boundary and
/// any remaining rings are interior holes (matching GeoJSON / `as_coordinates`).
fn rings_to_polygon(mut rings: Vec<LineString<f64>>) -> Polygon<f64> {
    if rings.is_empty() {
        Polygon::new(LineString::new(vec![]), vec![])
    } else {
        let exterior = rings.remove(0);
        Polygon::new(exterior, rings)
    }
}

// ---------------------------------------------------------------------------
// Read side: type discrimination + coordinate extraction
// ---------------------------------------------------------------------------

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Geometry_deleteInstance<'local>(
    _env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    release_instance::<Arc<Value>>(ptr);
    true as jboolean
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Geometry_isPoint<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || false as jboolean);
        if let Value::Geometry(g) = value.as_ref() {
            g.is_point() as jboolean
        } else {
            SurrealError::NullPointerException("Geometry").exception(env, || false as jboolean)
        }
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Geometry_getType<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, null_mut);
        if let Value::Geometry(g) = value.as_ref() {
            return new_string!(env, g.as_type(), null_mut);
        }
        SurrealError::NullPointerException("Geometry").exception(env, null_mut)
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Geometry_getPoint<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jdoubleArray {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, null_mut);
        if let Value::Geometry(Geometry::Point(pt)) = value.as_ref() {
            return new_double_point!(env, pt, null_mut);
        }
        SurrealError::NullPointerException("Geometry").exception(env, null_mut)
    })
}

/// Returns the geometry's coordinates as a nested SurrealDB `Value` (arrays of
/// `[x, y]` doubles), mirroring GeoJSON nesting. The Java side traverses this with
/// the existing `Value`/`Array` API. Not used for `GeometryCollection`, whose
/// per-child types must be preserved — see `getCollection`.
#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Geometry_getCoordinates<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlong {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || 0);
        if let Value::Geometry(g) = value.as_ref() {
            return JniTypes::new_value(Arc::new(g.as_coordinates()));
        }
        SurrealError::NullPointerException("Geometry").exception(env, || 0)
    })
}

/// Returns the children of a `GeometryCollection` as an array of `Value` pointers,
/// each a `Value::Geometry`, so the Java side can wrap them as `Geometry` and keep
/// each child's own type.
#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Geometry_getCollection<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jlongArray {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, null_mut);
        if let Value::Geometry(Geometry::Collection(geoms)) = value.as_ref() {
            let ptrs: Vec<jlong> = geoms
                .iter()
                .map(|g| JniTypes::new_value(Arc::new(Value::Geometry(g.clone()))))
                .collect();
            return new_jlong_array!(env, &ptrs, null_mut);
        }
        SurrealError::NullPointerException("GeometryCollection").exception(env, null_mut)
    })
}

// ---------------------------------------------------------------------------
// Write side: construct a Value::Geometry from Java coordinates. Each native
// returns a pointer to an `Arc<Value>` (a `Value::Geometry`), identical to the
// read-side representation, so a single `Geometry` Java class serves both and
// `deleteInstance` releases it the same way.
// ---------------------------------------------------------------------------

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Geometry_newPoint<'local>(
    _env: EnvUnowned<'local>,
    _class: JClass<'local>,
    x: jdouble,
    y: jdouble,
) -> jlong {
    JniTypes::new_value(Arc::new(Value::Geometry(Geometry::Point(Point::new(x, y)))))
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Geometry_newLineString<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    coords: JDoubleArray<'local>,
) -> jlong {
    with_env_body!(env, env, {
        let coords = unwrap_or_throw!(env, read_coords(env, coords));
        JniTypes::new_value(Arc::new(Value::Geometry(Geometry::Line(LineString::new(
            coords,
        )))))
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Geometry_newMultiPoint<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    coords: JDoubleArray<'local>,
) -> jlong {
    with_env_body!(env, env, {
        let coords = unwrap_or_throw!(env, read_coords(env, coords));
        let points: Vec<Point<f64>> = coords.into_iter().map(Point::from).collect();
        JniTypes::new_value(Arc::new(Value::Geometry(Geometry::MultiPoint(
            MultiPoint::new(points),
        ))))
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Geometry_newPolygon<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    rings: JObjectArray<'local, JDoubleArray<'local>>,
) -> jlong {
    with_env_body!(env, env, {
        let rings = unwrap_or_throw!(env, read_rings(env, rings));
        JniTypes::new_value(Arc::new(Value::Geometry(Geometry::Polygon(
            rings_to_polygon(rings),
        ))))
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Geometry_newMultiLineString<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    lines: JObjectArray<'local, JDoubleArray<'local>>,
) -> jlong {
    with_env_body!(env, env, {
        let lines = unwrap_or_throw!(env, read_rings(env, lines));
        JniTypes::new_value(Arc::new(Value::Geometry(Geometry::MultiLine(
            MultiLineString::new(lines),
        ))))
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Geometry_newMultiPolygon<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    polygons: JObjectArray<'local, JObjectArray<'local, JDoubleArray<'local>>>,
) -> jlong {
    with_env_body!(env, env, {
        let len = unwrap_or_throw!(env, polygons.len(env));
        let mut polys = Vec::with_capacity(len);
        for i in 0..len {
            let rings_arr = unwrap_or_throw!(env, polygons.get_element(env, i));
            let rings = unwrap_or_throw!(env, read_rings(env, rings_arr));
            polys.push(rings_to_polygon(rings));
        }
        JniTypes::new_value(Arc::new(Value::Geometry(Geometry::MultiPolygon(
            MultiPolygon::new(polys),
        ))))
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Geometry_newCollection<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptrs: JLongArray<'local>,
) -> jlong {
    with_env_body!(env, env, {
        let ptrs = get_long_array!(env, &ptrs, || 0);
        let mut geoms = Vec::with_capacity(ptrs.len());
        for p in ptrs {
            let value = get_value_instance!(env, p, || 0);
            if let Value::Geometry(g) = value.as_ref() {
                geoms.push(g.clone());
            } else {
                return SurrealError::NullPointerException("Geometry").exception(env, || 0);
            }
        }
        JniTypes::new_value(Arc::new(Value::Geometry(Geometry::Collection(geoms))))
    })
}

// ---------------------------------------------------------------------------
// Object identity (equals / hashCode / toString) — delegated to the SDK's
// `Geometry` impls, which already cover every variant.
// ---------------------------------------------------------------------------

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Geometry_equals<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr1: jlong,
    ptr2: jlong,
) -> jboolean {
    with_env_body!(env, env, {
        let v1 = get_value_instance!(env, ptr1, || false as jboolean);
        let v2 = get_value_instance!(env, ptr2, || false as jboolean);
        if let (Value::Geometry(g1), Value::Geometry(g2)) = (v1.as_ref(), v2.as_ref()) {
            return g1.eq(g2) as jboolean;
        }
        SurrealError::NullPointerException("Geometry").exception(env, || false as jboolean)
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Geometry_hashCode<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jint {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, || 0);
        if let Value::Geometry(g) = value.as_ref() {
            let mut hasher = DefaultHasher::new();
            g.hash(&mut hasher);
            let hash64 = hasher.finish();
            return (hash64 & 0xFFFFFFFF) as jint;
        }
        SurrealError::NullPointerException("Geometry").exception(env, || 0)
    })
}

#[no_mangle]
pub extern "system" fn Java_com_surrealdb_Geometry_toString<'local>(
    mut env: EnvUnowned<'local>,
    _class: JClass<'local>,
    ptr: jlong,
) -> jstring {
    with_env_body!(env, env, {
        let value = get_value_instance!(env, ptr, null_mut);
        if let Value::Geometry(g) = value.as_ref() {
            return new_string!(env, g.to_string(), null_mut);
        }
        SurrealError::NullPointerException("Geometry").exception(env, null_mut)
    })
}
