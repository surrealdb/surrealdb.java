package com.surrealdb.driver.geometry;

/**
 * Represents all geometry types SurrealDB supports, except for {@link GeometryCollection}.
 */
public sealed interface GeometryPrimitive permits LineString, MultiLineString, MultiPoint, MultiPolygon, Point, Polygon {

}
