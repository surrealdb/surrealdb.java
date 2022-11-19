package com.surrealdb.driver.geometry;

import com.surrealdb.meta.GeometryTest;
import com.surrealdb.meta.MultiGeometryTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MultiPolygonTest implements GeometryTest, MultiGeometryTest {

    @Test
    public void testEmptyConstantHasZeroElements() {
        assertEquals(0, MultiPolygon.EMPTY.getPolygonCount());
    }

    @Test
    public void testProvidingZeroElementsToFromGivesBackSingletonInstance() {
        assertSame(MultiPolygon.EMPTY, MultiPolygon.from());
        assertSame(MultiPolygon.EMPTY, MultiPolygon.from(List.of()));
    }

    @Test
    public void testToStringReturnsWKT() {
        Polygon poly1 = Polygon.from(
            LinearRing.from(
                Point.fromXY(0, 0),
                Point.fromXY(1, 0),
                Point.fromXY(1, 1),
                Point.fromXY(0, 1)
            )
        );

        Polygon poly2 = Polygon.withInteriorPolygons(
            LinearRing.from(
                Point.fromXY(-5, -1),
                Point.fromXY(3, 1),
                Point.fromXY(8, 12),
                Point.fromXY(-4, 5)
            ),
            List.of(
                LinearRing.from(
                    Point.fromXY(0.1, 0.1),
                    Point.fromXY(0.9, 0.1),
                    Point.fromXY(0.9, 0.9),
                    Point.fromXY(0.1, 0.9)
                )
            )
        );

        MultiPolygon multiPolygon = MultiPolygon.from(poly1, poly2);
        assertEquals("MULTIPOLYGON (((0 0, 1 0, 1 1, 0 1, 0 0)), ((-5 -1, 3 1, 8 12, -4 5, -5 -1), (0.1 0.1, 0.9 0.1, 0.9 0.9, 0.1 0.9, 0.1 0.1)))", multiPolygon.toString());
    }

    @Test
    public void testEqualsReturnsTrueForEqualObjects() {
        Polygon poly1 = Polygon.from(
            LinearRing.from(
                Point.fromXY(0, 0),
                Point.fromXY(1, 0),
                Point.fromXY(1, 1),
                Point.fromXY(0, 1)
            )
        );

        Polygon poly2 = Polygon.withInteriorPolygons(
            LinearRing.from(
                Point.fromXY(-5, -1),
                Point.fromXY(3, 1),
                Point.fromXY(8, 12),
                Point.fromXY(-4, 5)
            ),
            List.of(
                LinearRing.from(
                    Point.fromXY(0.1, 0.1),
                    Point.fromXY(0.9, 0.1),
                    Point.fromXY(0.9, 0.9),
                    Point.fromXY(0.1, 0.9)
                )
            )
        );

        MultiPolygon multiPolygon1 = MultiPolygon.from(poly1, poly2);
        MultiPolygon multiPolygon2 = MultiPolygon.from(poly1, poly2);

        assertEquals(multiPolygon1, multiPolygon2);
    }

    @Test
    public void testEqualsReturnsFalseForDifferentObjects() {
        Polygon poly1 = Polygon.from(
            LinearRing.from(
                Point.fromXY(0, 0),
                Point.fromXY(1, 0),
                Point.fromXY(1, 1),
                Point.fromXY(0, 1)
            )
        );

        Polygon poly2 = Polygon.withInteriorPolygons(
            LinearRing.from(
                Point.fromXY(-5, -1),
                Point.fromXY(3, 1),
                Point.fromXY(8, 12),
                Point.fromXY(-4, 5)
            ),
            List.of(
                LinearRing.from(
                    Point.fromXY(0.1, 0.1),
                    Point.fromXY(0.9, 0.1),
                    Point.fromXY(0.9, 0.9),
                    Point.fromXY(0.1, 0.9)
                )
            )
        );

        MultiPolygon multiPolygon1 = MultiPolygon.from(poly1, poly2);
        MultiPolygon multiPolygon2 = MultiPolygon.from(poly2, poly1);

        assertNotEquals(multiPolygon1, multiPolygon2);
    }

    @Test
    public void testHashCodeReturnsSameValueForEqualObjects() {
        Polygon poly1 = Polygon.from(
            LinearRing.from(
                Point.fromXY(0, 0),
                Point.fromXY(1, 0),
                Point.fromXY(1, 1),
                Point.fromXY(0, 1)
            )
        );

        Polygon poly2 = Polygon.withInteriorPolygons(
            LinearRing.from(
                Point.fromXY(-5, -1),
                Point.fromXY(3, 1),
                Point.fromXY(8, 12),
                Point.fromXY(-4, 5)
            ),
            List.of(
                LinearRing.from(
                    Point.fromXY(0.1, 0.1),
                    Point.fromXY(0.9, 0.1),
                    Point.fromXY(0.9, 0.9),
                    Point.fromXY(0.1, 0.9)
                )
            )
        );

        MultiPolygon multiPolygon1 = MultiPolygon.from(poly1, poly2);
        MultiPolygon multiPolygon2 = MultiPolygon.from(poly1, poly2);

        assertEquals(multiPolygon1.hashCode(), multiPolygon2.hashCode());
    }
}
