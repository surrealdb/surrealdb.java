package com.surrealdb.geometry;

import meta.tests.GeometryTests;
import meta.tests.MultiGeometryTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static meta.utils.GeometryUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class MultiPolygonTest extends MultiGeometryTest {

    @Test
    public void testEmptyConstantHasZeroElements() {
        assertEquals(0, MultiPolygon.EMPTY.getPolygonCount());
    }

    @Test
    public void testProvidingZeroElementsToFromGivesBackSingletonInstance() {
        assertSame(MultiPolygon.EMPTY, MultiPolygon.from());
        assertSame(MultiPolygon.EMPTY, MultiPolygon.from(List.of()));
    }

    @Nested
    class MultiGeometryTests extends MultiGeometryTest {

        @Test
        @Override
        public void testEmptyConstantHasZeroElements() {
            assertEquals(0, MultiPolygon.EMPTY.getPolygonCount());
        }

        @Test
        @Override
        public void testProvidingZeroElementsToFromGivesBackSingletonInstance() {
            assertSame(MultiPolygon.EMPTY, MultiPolygon.from());
            assertSame(MultiPolygon.EMPTY, MultiPolygon.from(List.of()));
            assertSame(MultiPolygon.EMPTY, MultiPolygon.builder().build());
        }
    }

    @Nested
    class StandardGeometryTests extends GeometryTests {

        @Override
        protected Geometry createSimpleGeometry() {
            return MultiPolygon.from(createQuadPolygon(true));
        }

        @Override
        protected Geometry createComplexGeometry() {
            return MultiPolygon.builder()
                .addPolygon(createCirclePolygon(24, 5))
                .addPolygon(createCirclePolygon(24, 10))
                .addPolygon(createQuadPolygonWithHole())
                .build();
        }

        @Test
        @Override
        public void toString_whenCalled_returnValidWkt() {
            Polygon poly1 = createQuadPolygon(false);
            Polygon poly2 = createQuadPolygonWithHole();

            MultiPolygon multiPolygon = MultiPolygon.from(poly1, poly2);
            assertEquals("MULTIPOLYGON (((-1 -1, 1 -1, 1 1, -1 1, -1 -1)), ((-1 -1, 1 -1, 1 1, -1 1, -1 -1), (-0.75 -0.75, -0.75 0.75, 0.75 0.75, 0.75 -0.75, -0.75 -0.75)))", multiPolygon.toString());
        }

        @Test
        @Override
        public void getPointCount_whenCalled_returnCorrectNumberOfPoints() {
            Polygon poly1 = createQuadPolygon(false);
            Polygon poly2 = createQuadPolygonWithHole();

            MultiPolygon multiPolygon = MultiPolygon.from(poly1, poly2);
            assertEquals(15, multiPolygon.getPointCount());
        }
    }
}
