package com.surrealdb;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * A SurrealDB geometry value. Supports all GeoJSON-style types: {@code Point},
 * {@code LineString}, {@code Polygon}, {@code MultiPoint}, {@code MultiLineString},
 * {@code MultiPolygon}, and {@code GeometryCollection}.
 * <p>
 * Coordinates use {@link java.awt.geom.Point2D.Double} where {@code x} is the
 * longitude and {@code y} is the latitude (GeoJSON ordering). Multi-coordinate
 * types are exposed as nested {@link List}s of points.
 * <p>
 * Instances can be read from query results (via {@link Value#getGeometry()}) and
 * constructed with the static factory methods to be sent back to the database.
 */
public class Geometry extends Native {

	Geometry(long ptr) {
		super(ptr);
	}

	// ---- native: read ----

	private static native boolean isPoint(long ptr);

	private static native double[] getPoint(long ptr);

	private static native String getType(long ptr);

	private static native long getCoordinates(long ptr);

	private static native long[] getCollection(long ptr);

	// ---- native: write ----

	private static native long newPoint(double x, double y);

	private static native long newLineString(double[] coords);

	private static native long newPolygon(double[][] rings);

	private static native long newMultiPoint(double[] coords);

	private static native long newMultiLineString(double[][] lines);

	private static native long newMultiPolygon(double[][][] polygons);

	private static native long newCollection(long[] geometryPtrs);

	@Override
	final native String toString(long ptr);

	@Override
	final native int hashCode(long ptr);

	@Override
	final native boolean equals(long ptr1, long ptr2);

	@Override
	final native void deleteInstance(long ptr);

	// ---- type discrimination ----

	/**
	 * Returns the geometry type: one of {@code "Point"}, {@code "LineString"},
	 * {@code "Polygon"}, {@code "MultiPoint"}, {@code "MultiLineString"},
	 * {@code "MultiPolygon"}, or {@code "GeometryCollection"}.
	 */
	public String getType() {
		return getType(getPtr());
	}

	public boolean isPoint() {
		return isPoint(getPtr());
	}

	public boolean isLineString() {
		return "LineString".equals(getType());
	}

	public boolean isPolygon() {
		return "Polygon".equals(getType());
	}

	public boolean isMultiPoint() {
		return "MultiPoint".equals(getType());
	}

	public boolean isMultiLineString() {
		return "MultiLineString".equals(getType());
	}

	public boolean isMultiPolygon() {
		return "MultiPolygon".equals(getType());
	}

	public boolean isGeometryCollection() {
		return "GeometryCollection".equals(getType());
	}

	// ---- readers ----

	public Point2D.Double getPoint() {
		final double[] coord = getPoint(getPtr());
		return new Point2D.Double(coord[0], coord[1]);
	}

	public List<Point2D.Double> getLineString() {
		return parseLine(coordinates());
	}

	public List<Point2D.Double> getMultiPoint() {
		return parseLine(coordinates());
	}

	/**
	 * Returns the polygon's rings. The first ring is the exterior boundary; any
	 * subsequent rings are interior holes.
	 */
	public List<List<Point2D.Double>> getPolygon() {
		return parseRings(coordinates());
	}

	public List<List<Point2D.Double>> getMultiLineString() {
		return parseRings(coordinates());
	}

	public List<List<List<Point2D.Double>>> getMultiPolygon() {
		final Array polygons = coordinates();
		final List<List<List<Point2D.Double>>> result = new ArrayList<>(polygons.len());
		for (int i = 0; i < polygons.len(); i++) {
			result.add(parseRings(polygons.get(i).getArray()));
		}
		return result;
	}

	public List<Geometry> getGeometryCollection() {
		final long[] ptrs = getCollection(getPtr());
		final List<Geometry> result = new ArrayList<>(ptrs.length);
		for (final long ptr : ptrs) {
			result.add(new Geometry(ptr));
		}
		return result;
	}

	// ---- factories (x = longitude, y = latitude) ----

	public static Geometry point(double x, double y) {
		return new Geometry(newPoint(x, y));
	}

	public static Geometry point(Point2D.Double point) {
		return new Geometry(newPoint(point.x, point.y));
	}

	public static Geometry lineString(List<Point2D.Double> points) {
		return new Geometry(newLineString(flatten(points)));
	}

	public static Geometry multiPoint(List<Point2D.Double> points) {
		return new Geometry(newMultiPoint(flatten(points)));
	}

	/**
	 * Builds a polygon. The first ring is the exterior boundary; any subsequent
	 * rings are interior holes.
	 */
	public static Geometry polygon(List<List<Point2D.Double>> rings) {
		return new Geometry(newPolygon(flattenRings(rings)));
	}

	public static Geometry multiLineString(List<List<Point2D.Double>> lines) {
		return new Geometry(newMultiLineString(flattenRings(lines)));
	}

	public static Geometry multiPolygon(List<List<List<Point2D.Double>>> polygons) {
		final double[][][] data = new double[polygons.size()][][];
		for (int i = 0; i < polygons.size(); i++) {
			data[i] = flattenRings(polygons.get(i));
		}
		return new Geometry(newMultiPolygon(data));
	}

	public static Geometry geometryCollection(List<Geometry> geometries) {
		final long[] ptrs = new long[geometries.size()];
		for (int i = 0; i < geometries.size(); i++) {
			ptrs[i] = geometries.get(i).getPtr();
		}
		return new Geometry(newCollection(ptrs));
	}

	// ---- helpers ----

	/** The geometry's coordinates as a nested array of {@code [x, y]} pairs. */
	private Array coordinates() {
		return new Value(getCoordinates(getPtr())).getArray();
	}

	private static Point2D.Double parsePoint(Value coordinate) {
		final Array xy = coordinate.getArray();
		return new Point2D.Double(xy.get(0).getDouble(), xy.get(1).getDouble());
	}

	private static List<Point2D.Double> parseLine(Array line) {
		final List<Point2D.Double> points = new ArrayList<>(line.len());
		for (int i = 0; i < line.len(); i++) {
			points.add(parsePoint(line.get(i)));
		}
		return points;
	}

	private static List<List<Point2D.Double>> parseRings(Array rings) {
		final List<List<Point2D.Double>> result = new ArrayList<>(rings.len());
		for (int i = 0; i < rings.len(); i++) {
			result.add(parseLine(rings.get(i).getArray()));
		}
		return result;
	}

	private static double[] flatten(List<Point2D.Double> points) {
		final double[] coords = new double[points.size() * 2];
		for (int i = 0; i < points.size(); i++) {
			coords[i * 2] = points.get(i).x;
			coords[i * 2 + 1] = points.get(i).y;
		}
		return coords;
	}

	private static double[][] flattenRings(List<List<Point2D.Double>> rings) {
		final double[][] data = new double[rings.size()][];
		for (int i = 0; i < rings.size(); i++) {
			data[i] = flatten(rings.get(i));
		}
		return data;
	}
}
