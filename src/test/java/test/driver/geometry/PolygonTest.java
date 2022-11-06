package test.driver.geometry;

import com.surrealdb.driver.geometry.LineString;
import com.surrealdb.driver.geometry.Point;
import com.surrealdb.driver.geometry.Polygon;
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
