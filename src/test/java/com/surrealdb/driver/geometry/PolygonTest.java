package com.surrealdb.driver.geometry;

import com.surrealdb.meta.GeometryTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PolygonTest implements GeometryTest {

    @Test
    void testBuildThrowsWhenExteriorHasNotBeenSet() {
        assertThrows(IllegalStateException.class, () -> Polygon.builder().build());
    }

    @Test
    void testBuildDoesNotThrowWhenExteriorHasBeenSet() {
        LineString exterior = LineString.from(
            Point.fromXY(0, 0),
            Point.fromXY(1, 0),
            Point.fromXY(1, 1),
            Point.fromXY(0, 1),
            Point.fromXY(0, 0)
        );

        assertDoesNotThrow(() -> Polygon.builder().setExterior(exterior).build());
    }

    @Test
    public void testToStringReturnsWKT() {
        LineString exterior = LineString.from(
            Point.fromXY(0, 0),
            Point.fromXY(1, 0),
            Point.fromXY(1, 1),
            Point.fromXY(0, 1),
            Point.fromXY(0, 0)
        );

        Polygon polygon = Polygon.builder().setExterior(exterior).build();

        assertEquals("POLYGON ((0 0, 1 0, 1 1, 0 1, 0 0))", polygon.toString());
    }

    @Test
    public void testEqualsReturnsTrueForEqualObjects() {

    }

    @Test
    public void testEqualsReturnsFalseForDifferentObjects() {

    }

    @Test
    public void testHashCodeReturnsSameValueForEqualObjects() {

    }
}
