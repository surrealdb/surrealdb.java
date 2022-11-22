package com.surrealdb.driver.geometry;

import com.surrealdb.meta.GeometryTest;
import com.surrealdb.meta.utils.GeometryUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.surrealdb.meta.utils.GeometryUtils.*;
import static org.junit.jupiter.api.Assertions.*;

public class LinearRingTest {

    @Test
    void testOpenRingIsAutoClosed() {
        LinearRing ring = createQuadLinearRing(true);

        assertEquals(5, ring.getPointCount());
        assertEquals(Point.fromXY(-1, -1), ring.getPoint(0), "First point");
        assertEquals(Point.fromXY(-1, -1), ring.getPoint(4), "Last point");
    }

    @Test
    void testCreatingALinearRingWithClosedPointsDoesNotChange() {
        LinearRing ring = createQuadLinearRing(false);

        assertEquals(5, ring.getPointCount());
        assertEquals(Point.fromXY(-1, -1), ring.getPoint(0), "First point");
        assertEquals(Point.fromXY(-1, -1), ring.getPoint(4), "Last point");
    }

    @Test
    void testGetCenterReturnsExpectedCenter() {
        LinearRing circleLinearRing = GeometryUtils.createCircleLinearRing(16, 5);

        assertPointEquals(Point.fromXY(0, 0), circleLinearRing.getCenter());
    }

    @Test
    void testGetCircumferenceReturnsExpectedCircumference() {
        LinearRing quadLinearRing = createQuadLinearRing(true);

        assertEquals(889, quadLinearRing.getCircumferenceInKilometers(), 1);
    }

    // Test that a new object is not created when calling toRing() on a LinearRing
    @Test
    void testToLinearRingReturnsSelf() {
        LinearRing ring = createQuadLinearRing(true);

        // Equivalent to ring == ring.toLinearRing()
        assertSame(ring, ring.toLinearRing());
    }

    @Nested
    class StandardGeometryTests implements GeometryTest {

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
            LinearRing ring1 = createQuadLinearRing(true);
            LinearRing ring2 = createQuadLinearRing(false);

            assertEquals(ring1, ring2);
        }

        @Test
        public void testEqualsReturnsFalseForDifferentObjects() {
            LinearRing ring1 = createCircleLinearRing(16, 5);
            LinearRing ring2 = createCircleLinearRing(8, 5);

            assertNotEquals(ring1, ring2);
        }

        @Test
        public void testHashCodeReturnsSameValueForEqualObjects() {
            LinearRing ring1 = createQuadLinearRing(true);
            LinearRing ring2 = createQuadLinearRing(false);

            assertEquals(ring1.hashCode(), ring2.hashCode());
        }
    }
}
