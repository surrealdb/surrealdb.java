package com.surrealdb.driver.geometry;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PolygonTest {

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
}
