package com.surrealdb.driver.geometry;

import com.google.common.collect.ImmutableList;
import com.surrealdb.meta.GeometryTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.surrealdb.meta.utils.GeometryUtils.*;
import static org.junit.jupiter.api.Assertions.*;

public class PolygonTest {

    @Test
    void testBuildThrowsWhenExteriorHasNotBeenSet() {
        assertThrows(IllegalStateException.class, () -> Polygon.builder().build());
    }

    @Test
    void testBuildDoesNotThrowWhenExteriorHasBeenSet() {
        LinearRing exterior = createQuadLinearRing(true);

        assertDoesNotThrow(() -> Polygon.builder().setExterior(exterior).build());
    }

    @Test
    void testTranslation() {
        Polygon polygon = createQuadPolygonWithHole();
        Polygon translated = polygon.translate(1, 2);

        assertAll(
            () -> assertEquals(1, translated.getInteriorCount(), "Hole count"),

            () -> assertEquals(Point.fromXY(0, 1), translated.getExterior().getPoint(0), "Exterior p1"),
            () -> assertEquals(Point.fromXY(0, 3), translated.getExterior().getPoint(1), "Exterior p2"),
            () -> assertEquals(Point.fromXY(2, 3), translated.getExterior().getPoint(2), "Exterior p3"),
            () -> assertEquals(Point.fromXY(2, 1), translated.getExterior().getPoint(3), "Exterior p4"),

            () -> assertEquals(Point.fromXY(0.25, 1.25), translated.getInterior(0).getPoint(0), "Interior 0 - p1"),
            () -> assertEquals(Point.fromXY(0.25, 2.75), translated.getInterior(0).getPoint(1), "Interior 0 - p2"),
            () -> assertEquals(Point.fromXY(1.75, 2.75), translated.getInterior(0).getPoint(2), "Interior 0 - p3"),
            () -> assertEquals(Point.fromXY(1.75, 1.25), translated.getInterior(0).getPoint(3), "Interior 0 - p4")
        );
    }

    @Nested
    class StandardGeometryTests implements GeometryTest {

        @Test
        @Override
        public void testToStringReturnsWKT() {
            ImmutableList<Polygon> quads = ImmutableList.of(
                createQuadPolygon(false),
                createQuadPolygon(true)
            );
            quads.forEach(polygon -> assertEquals("POLYGON ((-1 -1, -1 1, 1 1, 1 -1, -1 -1))", polygon.toString()));

            Polygon quadWithHole = createQuadPolygonWithHole();
            assertEquals("POLYGON ((-1 -1, -1 1, 1 1, 1 -1, -1 -1), (-0.75 -0.75, -0.75 0.75, 0.75 0.75, 0.75 -0.75, -0.75 -0.75))", quadWithHole.toString());
        }

        @Test
        @Override
        public void testGetPointCountReturnsCorrectCount() {
            Polygon quad = createQuadPolygon(false);
            assertEquals(5, quad.getPointCount());

            Polygon quadWithHole = createQuadPolygonWithHole();
            assertEquals(10, quadWithHole.getPointCount());
        }

        @Test
        @Override
        public void testEqualsReturnsTrueForEqualObjects() {
            Polygon poly1 = createQuadPolygon(true);
            Polygon poly2 = createQuadPolygon(false);
            assertEquals(poly1, poly2);
        }

        @Test
        @Override
        public void testEqualsReturnsFalseForDifferentObjects() {
            Polygon poly1 = createQuadPolygon(true);
            Polygon poly2 = createQuadPolygonWithHole();
            assertNotEquals(poly1, poly2);
        }

        @Test
        @Override
        public void testHashCodeReturnsSameValueForEqualObjects() {
            Polygon poly1 = createQuadPolygon(true);
            Polygon poly2 = createQuadPolygon(false);
            assertEquals(poly1.hashCode(), poly2.hashCode());
        }
    }
}
