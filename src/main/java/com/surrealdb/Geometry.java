package com.surrealdb;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * A SurrealDB geometry value. Supports all GeoJSON-style types: {@code Point},
 * {@code LineString}, {@code Polygon}, {@code MultiPoint},
 * {@code MultiLineString}, {@code MultiPolygon}, and
 * {@code GeometryCollection}.
 * <p>
 * Coordinates use {@link java.awt.geom.Point2D.Double} where {@code x} is the
 * longitude and {@code y} is the latitude (GeoJSON ordering). Multi-coordinate
 * types are exposed as nested {@link List}s of points.
 * <p>
 * Instances can be read from query results (via {@link Value#getGeometry()})
 * and constructed with the static factory methods to be sent back to the
 * database.
 */
public class Geometry extends Native {

	Geometry(long ptr) {
		super(ptr);
	}

	// ---- native: read ----

	private native boolean isPoint(long ptr);

	private native double[] getPoint(long ptr);

	private native String getType(long ptr);

	private native long getCoordinates(long ptr);

	private native long[] getCollection(long ptr);

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

	private static native void deleteInstance(long ptr);

	@Override
	final NativeCleaner.Deleter deleter() {
		return Geometry::deleteInstance;
	}

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
		try (final Array line = coordinates()) {
			return parseLine(line);
		}
	}

	public List<Point2D.Double> getMultiPoint() {
		try (final Array points = coordinates()) {
			return parseLine(points);
		}
	}

	/**
	 * Returns the polygon's rings. The first ring is the exterior boundary; any
	 * subsequent rings are interior holes.
	 */
	public List<List<Point2D.Double>> getPolygon() {
		try (final Array rings = coordinates()) {
			return parseRings(rings);
		}
	}

	public List<List<Point2D.Double>> getMultiLineString() {
		try (final Array lines = coordinates()) {
			return parseRings(lines);
		}
	}

	public List<List<List<Point2D.Double>>> getMultiPolygon() {
		try (final Array polygons = coordinates()) {
			final int len = polygons.len();
			final List<List<List<Point2D.Double>>> result = new ArrayList<>(len);
			for (int i = 0; i < len; i++) {
				try (final Array polygon = arrayAt(polygons, i)) {
					result.add(parseRings(polygon));
				}
			}
			return result;
		}
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
		// Snapshot once: the list may be lazy, linked, or changed concurrently.
		final Geometry[] array = geometries.toArray(new Geometry[0]);
		try {
			return new Geometry(newCollection(getPtrs(array)));
		} finally {
			// Fencing the array keeps every Geometry in it reachable.
			reachabilityFence(array);
		}
	}

	// ---- helpers ----

	// The parsers below close each native wrapper once its numbers are copied out.

	/** The geometry's coordinates as a nested array of {@code [x, y]} pairs. */
	private Array coordinates() {
		try (final Value coordinates = new Value(getCoordinates(getPtr()))) {
			return coordinates.getArray();
		}
	}

	/** Element {@code i} of {@code array}, which is itself an array. */
	private static Array arrayAt(Array array, int i) {
		try (final Value element = array.get(i)) {
			return element.getArray();
		}
	}

	private static Point2D.Double parsePoint(Array line, int i) {
		try (final Array xy = arrayAt(line, i); final Value x = xy.get(0); final Value y = xy.get(1)) {
			return new Point2D.Double(x.getDouble(), y.getDouble());
		}
	}

	private static List<Point2D.Double> parseLine(Array line) {
		final int len = line.len();
		final List<Point2D.Double> points = new ArrayList<>(len);
		for (int i = 0; i < len; i++) {
			points.add(parsePoint(line, i));
		}
		return points;
	}

	private static List<List<Point2D.Double>> parseRings(Array rings) {
		final int len = rings.len();
		final List<List<Point2D.Double>> result = new ArrayList<>(len);
		for (int i = 0; i < len; i++) {
			try (final Array ring = arrayAt(rings, i)) {
				result.add(parseLine(ring));
			}
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
