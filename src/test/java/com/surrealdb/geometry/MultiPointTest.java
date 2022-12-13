package com.surrealdb.geometry;

import meta.tests.GeometryTests;
import meta.tests.MultiGeometryTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MultiPointTest extends MultiGeometryTest {

    @Test
    public void testEmptyConstantHasZeroElements() {
        assertEquals(0, MultiPoint.EMPTY.getPointCount());
    }

    @Test
    public void testProvidingZeroElementsToFromGivesBackSingletonInstance() {
        assertEquals(MultiPoint.EMPTY, MultiPoint.from());
        assertEquals(MultiPoint.EMPTY, MultiPoint.from(List.of()));
    }

    @Nested
    class StandardGeometryTests extends GeometryTests {

        @Override
        protected Geometry createSimpleGeometry() {
            return MultiPoint.from(Point.fromXY(0, 0), Point.fromXY(1, 1));
        }

        @Override
        protected Geometry createComplexGeometry() {
            return MultiPoint.from(Point.fromXY(0, 0), Point.fromXY(1, 1), Point.fromXY(2, 2));
        }

        @Test
        @Override
        public void toString_whenCalled_returnValidWkt() {
            MultiPoint multiPoint = MultiPoint.from(
                Point.fromXY(-91.6711, -13.4225),
                Point.fromXY(-68.295, 7.24),
                Point.fromXY(3.9, 7)
            );

            assertEquals("MULTIPOINT (-91.6711 -13.4225, -68.295 7.24, 3.9 7)", multiPoint.toString());
        }

        @Test
        @Override
        public void getPointCount_whenCalled_returnCorrectNumberOfPoints() {
            MultiPoint multiPoint = MultiPoint.from(
                Point.fromXY(-91.6711, -13.4225),
                Point.fromXY(-68.295, 7.24),
                Point.fromXY(3.9, 7)
            );

            assertEquals(3, multiPoint.getPointCount());
        }
    }
}
