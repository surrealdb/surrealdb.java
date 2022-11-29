package com.surrealdb.driver.geometry;

import com.surrealdb.geometry.Geometry;
import com.surrealdb.geometry.LinearRing;
import com.surrealdb.geometry.Point;
import com.surrealdb.meta.GeometryTests;
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
    void getCenter_whenCalled_returnsCenterOfLinearRing() {
        LinearRing circleLinearRing = GeometryUtils.createCircleLinearRing(16, 5).translate(5, 5);

        assertPointEquals(Point.fromXY(5, 5), circleLinearRing.getCenter());
    }

    @Test
    void testGetCircumferenceReturnsExpectedCircumference() {
        LinearRing quadLinearRing = createQuadLinearRing(true);

        assertEquals(889, quadLinearRing.getCircumferenceInKilometers(), 1);
    }

    @Nested
    class StandardGeometryTests extends GeometryTests {

        @Override
        protected Geometry createSimpleGeometry() {
            return createQuadLinearRing(true);
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
            LinearRing circle = GeometryUtils.createCircleLinearRing(16, 5);

            assertEquals(17, circle.getPointCount());
        }
    }
}
