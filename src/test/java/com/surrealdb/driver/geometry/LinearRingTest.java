package com.surrealdb.driver.geometry;

import com.surrealdb.meta.GeometryTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LinearRingTest implements GeometryTest {

    @Test
    void testOpenRingIsAutoClosed() {
        LinearRing ring = LinearRing.from(
            Point.fromXY(0, 0),
            Point.fromXY(1, 0),
            Point.fromXY(1, 1),
            Point.fromXY(0, 1)
        );

        assertEquals(5, ring.getPointCount());
        assertEquals(Point.fromXY(0, 0), ring.getPoint(0), "First point");
        assertEquals(Point.fromXY(0, 0), ring.getPoint(4), "Last point");
    }

    @Test
    void testClosedRingDoesNotChange() {
        LinearRing ring = LinearRing.from(
            Point.fromXY(0, 0),
            Point.fromXY(1, 0),
            Point.fromXY(1, 1),
            Point.fromXY(0, 1),
            Point.fromXY(0, 0)
        );

        assertEquals(5, ring.getPointCount());
        assertEquals(Point.fromXY(0, 0), ring.getPoint(0), "First point");
        assertEquals(Point.fromXY(0, 0), ring.getPoint(4), "Last point");
    }

    // Test that a new object is not created when calling toRing() on a LinearRing
    @Test
    void testToLinearRingReturnsSelf() {
        LinearRing ring = LinearRing.from(
            Point.fromXY(0, 0),
            Point.fromXY(1, 0),
            Point.fromXY(1, 1),
            Point.fromXY(0, 1),
            Point.fromXY(0, 0)
        );

        // Equivalent to ring == ring.toLinearRing()
        assertSame(ring, ring.toLinearRing());
    }

    @Test
    public void testToStringReturnsWKT() {
        LinearRing ring = LinearRing.from(
            Point.fromXY(0, 0),
            Point.fromXY(1, 0),
            Point.fromXY(1, 1),
            Point.fromXY(0, 1)
        );

        assertEquals("LINESTRING (0 0, 1 0, 1 1, 0 1, 0 0)", ring.toString());
    }

    @Test
    public void testEqualsReturnsTrueForEqualObjects() {
        LinearRing ring1 = LinearRing.from(
            Point.fromXY(0, 0),
            Point.fromXY(1, 0),
            Point.fromXY(1, 1),
            Point.fromXY(0, 1)
        );
        LinearRing ring2 = LinearRing.from(
            Point.fromXY(0, 0),
            Point.fromXY(1, 0),
            Point.fromXY(1, 1),
            Point.fromXY(0, 1),
            Point.fromXY(0, 0)
        );

        assertEquals(ring1, ring2);
    }

    @Test
    public void testEqualsReturnsFalseForDifferentObjects() {
        LinearRing ring1 = LinearRing.from(
            Point.fromXY(0, 0),
            Point.fromXY(1, 0),
            Point.fromXY(1, 1),
            Point.fromXY(0, 1)
        );
        LinearRing ring2 = LinearRing.from(
            Point.fromXY(0, 0),
            Point.fromXY(1, 0),
            Point.fromXY(1, 1),
            Point.fromXY(0, 1),
            Point.fromXY(3, 5)
        );

        assertNotEquals(ring1, ring2);
    }

    @Test
    public void testHashCodeReturnsSameValueForEqualObjects() {
        LinearRing ring1 = LinearRing.from(
            Point.fromXY(0, 0),
            Point.fromXY(1, 0),
            Point.fromXY(1, 1),
            Point.fromXY(0, 1)
        );
        LinearRing ring2 = LinearRing.from(
            Point.fromXY(0, 0),
            Point.fromXY(1, 0),
            Point.fromXY(1, 1),
            Point.fromXY(0, 1),
            Point.fromXY(0, 0)
        );

        assertEquals(ring1.hashCode(), ring2.hashCode());
    }
}
