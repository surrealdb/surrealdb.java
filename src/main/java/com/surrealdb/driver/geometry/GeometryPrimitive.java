package com.surrealdb.driver.geometry;

/**
 * Represents all geometry types SurrealDB supports, except for {@link GeometryCollection}.
 */
public sealed abstract class GeometryPrimitive extends Geometry permits LineString, LinearRing, MultiLineString, MultiPoint, MultiPolygon, Point, Polygon {

}
