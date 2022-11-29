package com.surrealdb.driver.geometry;

import com.surrealdb.geometry.Geometry;
import com.surrealdb.geometry.MultiPolygon;
import com.surrealdb.geometry.Polygon;
import com.surrealdb.meta.GeometryTests;
import com.surrealdb.meta.MultiGeometryTest;
import com.surrealdb.meta.utils.GeometryUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.surrealdb.meta.utils.GeometryUtils.*;
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
            Polygon poly1 = GeometryUtils.createQuadPolygon(false);
            Polygon poly2 = createQuadPolygonWithHole();

            MultiPolygon multiPolygon = MultiPolygon.from(poly1, poly2);
            assertEquals("MULTIPOLYGON (((-1 -1, -1 1, 1 1, 1 -1, -1 -1)), ((-1 -1, -1 1, 1 1, 1 -1, -1 -1), (-0.75 -0.75, -0.75 0.75, 0.75 0.75, 0.75 -0.75, -0.75 -0.75)))", multiPolygon.toString());
        }

        @Test
        @Override
        public void getPointCount_whenCalled_returnCorrectNumberOfPoints() {
            Polygon poly1 = GeometryUtils.createQuadPolygon(false);
            Polygon poly2 = createQuadPolygonWithHole();

            MultiPolygon multiPolygon = MultiPolygon.from(poly1, poly2);
            assertEquals(15, multiPolygon.getPointCount());
        }
    }
}
