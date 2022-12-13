package com.surrealdb.geometry;

import meta.tests.GeometryTests;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static meta.utils.GeometryUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LinearRingTest {

    @Test
    void testOpenRingIsAutoClosed() {
        LinearRing ring = createQuadLinearRing(true, true);

        assertEquals(5, ring.getPointCount());
        assertEquals(Point.fromXY(-1, -1), ring.getPoint(0), "First point");
        assertEquals(Point.fromXY(-1, -1), ring.getPoint(4), "Last point");
    }

    @Test
    void testCreatingALinearRingWithClosedPointsDoesNotChange() {
        LinearRing ring = createQuadLinearRing(true, false);

        assertEquals(5, ring.getPointCount());
        assertEquals(Point.fromXY(-1, -1), ring.getPoint(0), "First point");
        assertEquals(Point.fromXY(-1, -1), ring.getPoint(4), "Last point");
    }

    @Test
    void getCenter_whenCalled_returnsCenterOfLinearRing() {
        LinearRing circleLinearRing = createCircleLinearRing(16, 5).translate(5, 5);

        assertPointEquals(Point.fromXY(5, 5), circleLinearRing.getCenter());
    }

    @Test
    void testGetCircumferenceReturnsExpectedCircumference() {
        LinearRing quadLinearRing = createQuadLinearRing(true, true);

        assertEquals(889, quadLinearRing.getCircumferenceInKilometers(), 1);
    }

    @Nested
    class StandardGeometryTests extends GeometryTests {

        @Override
        protected Geometry createSimpleGeometry() {
            return createQuadLinearRing(true, true);
        }

        @Override
        protected Geometry createComplexGeometry() {
            return createCircleLinearRing(24, 5);
        }

        @Test
        public void toString_whenCalled_returnValidWkt() {
            LinearRing ring = LinearRing.from(
                Point.fromXY(0, 0),
                Point.fromXY(1, 0),
                Point.fromXY(1, 1),
                Point.fromXY(0, 1)
            );

            assertEquals("LINEARRING (0 0, 1 0, 1 1, 0 1, 0 0)", ring.toString());
        }

        @Test
        public void getPointCount_whenCalled_returnCorrectNumberOfPoints() {
            LinearRing circle = createCircleLinearRing(16, 5);

            assertEquals(17, circle.getPointCount());
        }
    }
}
