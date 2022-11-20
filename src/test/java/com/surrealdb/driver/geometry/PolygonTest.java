package com.surrealdb.driver.geometry;

import com.surrealdb.meta.GeometryTest;
import org.junit.jupiter.api.Test;

import static com.surrealdb.meta.utils.GeometryUtils.*;
import static org.junit.jupiter.api.Assertions.*;

public class PolygonTest implements GeometryTest {

    @Test
    void testBuildThrowsWhenExteriorHasNotBeenSet() {
        assertThrows(IllegalStateException.class, () -> Polygon.builder().build());
    }

    @Test
    void testBuildDoesNotThrowWhenExteriorHasBeenSet() {
        LineString exterior = createQuadLinearRing(true);

        assertDoesNotThrow(() -> Polygon.builder().setExterior(exterior).build());
    }

    @Test
    public void testToStringReturnsWKT() {
        Polygon quad = createQuadPolygon(true);
        assertEquals("POLYGON ((0 0, 0 1, 1 1, 1 0, 0 0))", quad.toString());

        Polygon quadWithHole = createQuadPolygonWithHole();
        assertEquals("POLYGON ((0 0, 0 1, 1 1, 1 0, 0 0), (0.1 0.1, 0.1 0.9, 0.9 0.9, 0.9 0.1, 0.1 0.1))", quadWithHole.toString());
    }

    @Test
    public void testEqualsReturnsTrueForEqualObjects() {
        Polygon poly1 = createQuadPolygon(true);
        Polygon poly2 = createQuadPolygon(false);
        assertEquals(poly1, poly2);
    }

    @Test
    public void testEqualsReturnsFalseForDifferentObjects() {
        Polygon poly1 = createQuadPolygon(true);
        Polygon poly2 = createQuadPolygonWithHole();
        assertNotEquals(poly1, poly2);
    }

    @Test
    public void testHashCodeReturnsSameValueForEqualObjects() {
        Polygon poly1 = createQuadPolygon(true);
        Polygon poly2 = createQuadPolygon(false);
        assertEquals(poly1.hashCode(), poly2.hashCode());
    }
}
