package com.surrealdb.driver.geometry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PointTest {

    private static final double GEO_HASH_PRECISION = 0.00000001;

    @Test
    void testFromXY() {
        Point point = Point.fromXY(3, 5);

        assertEquals(3, point.getX());
        assertEquals(5, point.getY());
    }

    @Test
    void testFromYX() {
        Point point = Point.fromYX(5, 3);

        assertEquals(3, point.getX());
        assertEquals(5, point.getY());
    }

    @Test
    void testFromGeoHash() {
        assertPointEquals(Point.fromYX(51.50070948, -0.12456732), Point.fromGeoHash("gcpuvpmm3k5f"));
        assertPointEquals(Point.fromYX(29.97923900, 31.13425897), Point.fromGeoHash("stq4s3x38z4n"));
        assertPointEquals(Point.fromYX(37.81962781, -122.47855028), Point.fromGeoHash("9q8zhuvg6cte"));
    }

    void assertPointEquals(Point expected, Point actual) {
        assertEquals(expected.getX(), actual.getX(), PointTest.GEO_HASH_PRECISION);
        assertEquals(expected.getY(), actual.getY(), PointTest.GEO_HASH_PRECISION);
    }

    @Test
    void testToGeoHash() {
        // You can use http://geohash.co/ to verify the correctness of the assertions.
        // The site looks a little sketchy, so I'll find a better one later.
        assertEquals("u09tunqtwdtx", Point.fromYX(48.85853327, 2.29436914).toGeoHash(12));
        assertEquals("r3gx2ux9fyr3", Point.fromYX(-33.85678251, 151.21526157).toGeoHash(12));
        assertEquals("7zzzzzzzzzzz", Point.fromYX(0, 0).toGeoHash(12));
    }

    @Test
    void testWithX() {
        Point point = Point.fromXY(3, 5).withX(7);

        assertEquals(7, point.getX());
        assertEquals(5, point.getY());
    }

    @Test
    void testWithY() {
        Point point = Point.fromXY(3, 5).withY(7);

        assertEquals(3, point.getX());
        assertEquals(7, point.getY());
    }

    @Test
    void testEquality() {
        Point point1 = Point.fromXY(23, 73);
        Point point2 = Point.fromXY(23, 73);

        assertEquals(point1, point2);
        assertEquals(point1.hashCode(), point2.hashCode());
    }
}
