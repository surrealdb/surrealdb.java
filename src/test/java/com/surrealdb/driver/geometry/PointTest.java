package com.surrealdb.driver.geometry;

import com.surrealdb.meta.GeometryTests;
import com.surrealdb.meta.model.City;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.surrealdb.meta.utils.GeometryUtils.assertPointEquals;
import static org.junit.jupiter.api.Assertions.*;

public class PointTest {

    @Test
    void Point_fromXY_whenProvidedWithCoords_returnsAPointWithThoseCoords() {
        Point point = Point.fromXY(3, 5);

        assertEquals(3, point.getX());
        assertEquals(5, point.getY());
    }

    @Test
    void Point_fromYX_whenProvidedWithCoords_returnsAPointWithThoseCoords() {
        Point point = Point.fromYX(5, 3);

        assertEquals(3, point.getX());
        assertEquals(5, point.getY());
    }

    @Test
    void Point_fromGeoHash_whenGivenAValidGeoHash_returnsMatchingPoint() {
        // You can use http://geohash.org/ to verify the correctness of the assertions.
        assertPointEquals(Point.fromYX(51.50070948, -0.12456732), Point.fromGeoHash("gcpuvpmm3k5f"));
        assertPointEquals(Point.fromYX(29.97923900, 31.13425897), Point.fromGeoHash("stq4s3x38z4n"));
        assertPointEquals(Point.fromYX(37.81962781, -122.47855028), Point.fromGeoHash("9q8zhuvg6cte"));
    }

    @Test
    void Point_fromGeoHash_whenGivenAnInvalidGeoHash_throwException() {
        assertThrows(IllegalArgumentException.class, () -> Point.fromGeoHash("B"), "capital letters are not allowed.");
        assertThrows(IllegalArgumentException.class, () -> Point.fromGeoHash("a"), "'a' is not a valid character.");
        assertThrows(IllegalArgumentException.class, () -> Point.fromGeoHash("i"), "'i' is not a valid character.");
        assertThrows(IllegalArgumentException.class, () -> Point.fromGeoHash("l"), "'l' is not a valid character.");
        assertThrows(IllegalArgumentException.class, () -> Point.fromGeoHash("o"), "'o' is not a valid character.");
    }

    @Test
    void toGeoHash_whenCalled_returnsAMatchingGeoHash() {
        // You can use http://geohash.co/ to verify the correctness of the assertions.
        // The site looks a little sketchy, so I'll find a better one later.
        assertEquals("u09tunqtwdtx", Point.fromYX(48.85853327, 2.29436914).toGeoHash(12));
        assertEquals("r3gx2ux9fyr3", Point.fromYX(-33.85678251, 151.21526157).toGeoHash(12));
        assertEquals("7zzzzzzzzzzz", Point.fromYX(0, 0).toGeoHash(12));
    }

    @Test
    void withX_whenCalled_returnsAPointWithTheProvidedX() {
        Point point = Point.fromXY(3, 5).withX(7);

        assertEquals(7, point.getX());
        assertEquals(5, point.getY());
    }

    @Test
    void withY_whenCalled_returnsAPointWithTheProvidedY() {
        Point point = Point.fromXY(3, 5).withY(7);

        assertEquals(3, point.getX());
        assertEquals(7, point.getY());
    }

    @Test
    void rotate_whenCalled_returnsARotatedPoint() {
        Point point = Point.fromXY(0, 0);
        assertPointEquals(point, point.rotateDegrees(90),"Rot: 90");
        assertPointEquals(point, point.rotateDegrees(180), "Rot: 180");
        assertPointEquals(point, point.rotateDegrees(270), "Rot: 270");
        assertPointEquals(point, point.rotateDegrees(360), "Rot: 360");

        point = Point.fromXY(50, 0);
        assertPointEquals(Point.fromXY(35.355339059327, -35.355339059327), point.rotateDegrees(-45), "Rot: -45");
        assertPointEquals(Point.fromXY(35.355339059327, -35.355339059327), point.rotateDegrees(315), "Rot: 315");

        assertPointEquals(Point.fromXY(0, -50), point.rotateDegrees(-90), "Rot: -90");
        assertPointEquals(Point.fromXY(0, -50), point.rotateDegrees(270), "Rot: 270");

        assertPointEquals(Point.fromXY(-35.355339059327, -35.355339059327), point.rotateDegrees(-135), "Rot: -135");
        assertPointEquals(Point.fromXY(-35.355339059327, -35.355339059327), point.rotateDegrees(225), "Rot: 225");

        assertPointEquals(Point.fromXY(-50, 0), point.rotateDegrees(-180), "Rot: -180");
        assertPointEquals(Point.fromXY(-50, 0), point.rotateDegrees(180), "Rot: 180");

        assertPointEquals(Point.fromXY(-35.355339059327, 35.355339059327), point.rotateDegrees(-225), "Rot: -225");
        assertPointEquals(Point.fromXY(-35.355339059327, 35.355339059327), point.rotateDegrees(135), "Rot: 135");

        assertPointEquals(Point.fromXY(0, 50), point.rotateDegrees(-270), "Rot: -270");
        assertPointEquals(Point.fromXY(0, 50), point.rotateDegrees(90), "Rot: 90");

        assertPointEquals(Point.fromXY(35.355339059327, 35.355339059327), point.rotateDegrees(-315), "Rot: -315");
        assertPointEquals(Point.fromXY(35.355339059327, 35.355339059327), point.rotateDegrees(45), "Rot: 45");

        assertPointEquals(Point.fromXY(50, 0), point.rotateDegrees(-360), "Rot: -360");
        assertPointEquals(Point.fromXY(50, 0), point.rotateDegrees(0), "Rot: 0");
    }

    @Test
    void testDistanceInMeters() {
        // Use https://www.omnicalculator.com/other/latitude-longitude-distance to verify the correctness of the assertions.
        Point p1 = Point.fromXY(0, 0);
        Point p2 = Point.fromXY(1, 0);
        assertEquals(111_195, p1.distanceInMeters(p2), 1);

        p1 = Point.fromXY(0, 0);
        p2 = Point.fromXY(0, 1);
        assertEquals(111_195, p1.distanceInMeters(p2), 1);

        p1 = Point.fromXY(96, 64);
        p2 = Point.fromXY(48, 32);
        assertEquals(4840, p1.distanceInKilometers(p2), 1);
    }

    @Nested
    class StandardGeometryTests extends GeometryTests {

        @Override
        protected Geometry createSimpleGeometry() {
            return Point.fromXY(3, -5);
        }

        @Override
        protected Geometry createComplexGeometry() {
            return City.NEW_YORK.getLocation();
        }

        @Test
        @Override
        public void toString_whenCalled_returnValidWkt() {
            assertEquals("POINT (3.141592653589793 2.718281828459045)", Point.fromXY(Math.PI, Math.E).toString(), "Decimal precision");
            assertEquals("POINT (0 0)", Point.fromXY(0, 0).toString(), "Zero");
            assertEquals("POINT (-1 -1)", Point.fromXY(-1, -1).toString(), "Negative");
        }

        @Test
        @Override
        public void getPointCount_whenCalled_returnCorrectNumberOfPoints() {
            assertEquals(1, Point.fromXY(0, 0).getPointCount());
            assertEquals(1, Point.fromXY(5, -10).getPointCount());
        }
    }
}
