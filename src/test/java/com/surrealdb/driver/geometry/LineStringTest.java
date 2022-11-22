package com.surrealdb.driver.geometry;

import com.google.common.collect.ImmutableList;
import com.surrealdb.meta.GeometryTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LineStringTest {

    @Test
    void testPointWithoutEnoughPoints() {
        assertThrows(IllegalArgumentException.class, () -> LineString.builder().build());
        assertThrows(IllegalArgumentException.class, () -> LineString.from(Point.fromXY(1, 2)));
    }

    @Test
    void testLineWithTwoPoints() {
        Point p1 = Point.fromXY(1, 2);
        Point p2 = Point.fromXY(3, 4);

        LineString line = LineString.from(p1, p2);

        assertEquals(2, line.getPointCount());
        assertEquals(p1, line.getPoint(0));
        assertEquals(p2, line.getPoint(1));
    }

    @Test
    void testImmutability() {
        Point p1 = Point.fromXY(1, 2);
        Point p2 = Point.fromXY(3, 4);

        LineString line = LineString.from(p1, p2);

        assertThrows(UnsupportedOperationException.class, () -> line.iterator().remove());
    }

    @Test
    void testToBuilder() {
        LineString line = LineString.from(Point.fromXY(1, 2), Point.fromXY(3, 4));
        LineString copy = line.toBuilder().build();

        assertEquals(line, copy);
    }

    @Test
    void testBuilderAddPoints() {
        LineString line = LineString.builder()
            .addPoint(Point.fromXY(1, 2))
            .addPointXY(3, 4)
            .addPointYX(6, 5)
            .addPoints(ImmutableList.of(Point.fromXY(7, 8), Point.fromXY(9, 10)))
            .addPoints(Point.fromXY(11, 12), Point.fromXY(13, 14))
            .build();

        assertEquals(7, line.getPointCount());
        assertEquals(Point.fromXY(1, 2), line.getPoint(0));
        assertEquals(Point.fromXY(3, 4), line.getPoint(1));
        assertEquals(Point.fromXY(5, 6), line.getPoint(2));
        assertEquals(Point.fromXY(7, 8), line.getPoint(3));
        assertEquals(Point.fromXY(9, 10), line.getPoint(4));
        assertEquals(Point.fromXY(11, 12), line.getPoint(5));
        assertEquals(Point.fromXY(13, 14), line.getPoint(6));
    }

    @Test
    void testBuilderRemovePoints() {
        Point p1 = Point.fromXY(1, 2);
        Point p2 = Point.fromXY(3, 4);
        Point p3 = Point.fromXY(5, 6);
        Point p4 = Point.fromXY(7, 8);
        Point p5 = Point.fromXY(9, 10);
        Point p6 = Point.fromXY(11, 12);
        Point p7 = Point.fromXY(13, 14);

        LineString line = LineString.from(p1, p2, p3, p4, p5, p6, p7)
            .toBuilder()
            .removePoint(p2)
            .removePoints(ImmutableList.of(p3, p5))
            .removePoints(p6, p7)
            .build();

        assertEquals(2, line.getPointCount());
        assertEquals(p1, line.getPoint(0));
        assertEquals(p4, line.getPoint(1));
    }

    @Test
    void testFlip() {
        LineString line = LineString.from(Point.fromXY(1, 2), Point.fromXY(3, 4), Point.fromXY(5, 6));
        LineString flipped = line.flip();

        System.out.println(line);
        System.out.println(flipped);

        int pointCount = line.getPointCount();
        assertEquals(3, pointCount);

        for (int i = 0; i < pointCount; i++) {
            Point originalPoint = line.getPoint(i);
            Point flippedPoint = flipped.getPoint(pointCount - i - 1);
            assertEquals(originalPoint, flippedPoint);
        }
    }

    @Nested
    class StandardGeometryTests implements GeometryTest {

        @Test
        public void testToStringReturnsWKT() {
            LineString line = LineString.from(Point.fromXY(1, 2), Point.fromXY(3, 4), Point.fromXY(5, 6));
            assertEquals("LINESTRING (1 2, 3 4, 5 6)", line.toString());
        }

        @Test
        public void testEqualsReturnsTrueForEqualObjects() {
            LineString line1 = LineString.from(Point.fromXY(1, 2), Point.fromXY(3, 4), Point.fromXY(5, 6));
            LineString line2 = LineString.from(Point.fromXY(1, 2), Point.fromXY(3, 4), Point.fromXY(5, 6));
            assertEquals(line1, line2);
        }

        @Test
        public void testEqualsReturnsFalseForDifferentObjects() {
            LineString line1 = LineString.from(
                Point.fromXY(1, 2),
                Point.fromXY(3, 4),
                Point.fromXY(5, 6)
            );
            LineString line2 = LineString.from(
                Point.fromXY(5, 6),
                Point.fromXY(3, 4),
                Point.fromXY(1, 2)
            );

            assertNotEquals(line1, line2);
        }

        @Test
        public void testHashCodeReturnsSameValueForEqualObjects() {
            LineString line1 = LineString.from(Point.fromXY(1, 2), Point.fromXY(3, 4), Point.fromXY(5, 6));
            LineString line2 = LineString.from(Point.fromXY(1, 2), Point.fromXY(3, 4), Point.fromXY(5, 6));
            assertEquals(line1.hashCode(), line2.hashCode());
        }
    }
}
