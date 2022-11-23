package com.surrealdb.driver.geometry;

import com.surrealdb.meta.GeometryTest;
import com.surrealdb.meta.MultiGeometryTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class MultiLineStringTest implements MultiGeometryTest {

    @Test
    public void testEmptyConstantHasZeroElements() {
        assertEquals(0, MultiLineString.EMPTY.getLineCount());
    }

    @Test
    public void testProvidingZeroElementsToFromGivesBackSingletonInstance() {
        assertSame(MultiLineString.EMPTY, MultiLineString.from());
        assertSame(MultiLineString.EMPTY, MultiLineString.from(List.of()));
        assertSame(MultiLineString.EMPTY, MultiLineString.builder().build());
    }

    @Nested
    class StandardGeometryTests implements GeometryTest {

        @Test
        @Override
        public void testToStringReturnsWKT() {
            assertEquals("MULTILINESTRING EMPTY", MultiLineString.EMPTY.toString());
            assertEquals("MULTILINESTRING ((1 2, 3 4))", MultiLineString.from(LineString.from(Point.fromXY(1, 2), Point.fromXY(3, 4))).toString());
            assertEquals("MULTILINESTRING ((1 2, 3 4), (5 6, 7 8))", MultiLineString.from(LineString.from(Point.fromXY(1, 2), Point.fromXY(3, 4)), LineString.from(Point.fromXY(5, 6), Point.fromXY(7, 8))).toString());
        }

        @Test
        @Override
        public void testEqualsReturnsTrueForEqualObjects() {

        }

        @Test
        @Override
        public void testEqualsReturnsFalseForDifferentObjects() {

        }

        @Test
        @Override
        public void testHashCodeReturnsSameValueForEqualObjects() {

        }
    }
}
