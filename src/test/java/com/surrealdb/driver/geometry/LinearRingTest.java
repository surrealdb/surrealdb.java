package com.surrealdb.driver.geometry;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class LinearRingTest {

    @Test
    void testOpenRingIsAutoClosed() {
        LineString ring = LinearRing.from(
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
        LineString ring = LinearRing.from(
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
    @Disabled
    void testToStringReturnsWKT() {
        LinearRing ring = LinearRing.from(
            Point.fromXY(0, 0),
            Point.fromXY(1, 0),
            Point.fromXY(1, 1),
            Point.fromXY(0, 1)
        );

        assertEquals("LINESTRING (0.0 0.0, 1.0 0.0, 1.0 1.0, 0.0 1.0, 0.0 0.0)", ring.toString());
    }
}
