package com.surrealdb.driver.geometry;

import com.surrealdb.meta.GeometryTest;
import com.surrealdb.meta.MultiGeometryTest;
import com.surrealdb.meta.utils.GeometryUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Supplier;

import static com.surrealdb.meta.utils.GeometryUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class MultiPolygonTest implements MultiGeometryTest {

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
    class MultiGeometryTests implements MultiGeometryTest {

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
    class StandardGeometryTests implements GeometryTest {

        @Test
        @Override
        public void testToStringReturnsWKT() {
            Polygon poly1 = GeometryUtils.createQuadPolygon(false);
            Polygon poly2 = createQuadPolygonWithHole();

            MultiPolygon multiPolygon = MultiPolygon.from(poly1, poly2);
            assertEquals("MULTIPOLYGON (((-1 -1, -1 1, 1 1, 1 -1, -1 -1)), ((-1 -1, -1 1, 1 1, 1 -1, -1 -1), (-0.75 -0.75, -0.75 0.75, 0.75 0.75, 0.75 -0.75, -0.75 -0.75)))", multiPolygon.toString());
        }

        @Test
        @Override
        public void testToStringReturnsCachedString() {
            Polygon poly1 = GeometryUtils.createQuadPolygon(false);
            Polygon poly2 = createQuadPolygonWithHole();

            MultiPolygon multiPolygon = MultiPolygon.from(poly1, poly2);

            String first = multiPolygon.toString();
            String second = multiPolygon.toString();

            assertSame(first, second);
        }

        @Test
        @Override
        public void testGetPointCountReturnsCorrectCount() {
            Polygon poly1 = GeometryUtils.createQuadPolygon(false);
            Polygon poly2 = createQuadPolygonWithHole();

            MultiPolygon multiPolygon = MultiPolygon.from(poly1, poly2);
            assertEquals(15, multiPolygon.getPointCount());
        }

        @Test
        @Override
        public void testEqualsReturnsTrueForEqualObjects() {
            Supplier<MultiPolygon> supplier = () -> MultiPolygon.from(
                createCirclePolygon(10, 1),
                createQuadPolygonWithHole()
            );

            MultiPolygon multiPolygon1 = supplier.get();
            MultiPolygon multiPolygon2 = supplier.get();

            assertEquals(multiPolygon1, multiPolygon2);
        }

        @Test
        @Override
        public void testEqualsReturnsFalseForDifferentObjects() {
            MultiPolygon multiPolygon1 = MultiPolygon.builder()
                .addPolygon(createCirclePolygon(4, 2))
                .addPolygon(createQuadPolygon(false))
                .addPolygon(createQuadPolygonWithHole())
                .build();
            MultiPolygon multiPolygon2 = MultiPolygon.builder()
                .addPolygon(createQuadPolygonWithHole())
                .build();

            assertNotEquals(multiPolygon1, multiPolygon2);
        }

        @Test
        @Override
        public void testHashCodeReturnsSameValueForEqualObjects() {
            Supplier<MultiPolygon> supplier = () -> MultiPolygon.from(
                createCirclePolygon(12, 4),
                createQuadPolygonWithHole(),
                createQuadPolygon(false)
            );

            MultiPolygon multiPolygon1 = supplier.get();
            MultiPolygon multiPolygon2 = supplier.get();

            assertEquals(multiPolygon1.hashCode(), multiPolygon2.hashCode());
        }
    }
}
