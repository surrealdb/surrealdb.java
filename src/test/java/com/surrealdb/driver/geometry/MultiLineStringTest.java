package com.surrealdb.driver.geometry;

import com.surrealdb.geometry.Geometry;
import com.surrealdb.geometry.LineString;
import com.surrealdb.geometry.MultiLineString;
import com.surrealdb.geometry.Point;
import com.surrealdb.meta.GeometryTests;
import com.surrealdb.meta.MultiGeometryTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class MultiLineStringTest extends MultiGeometryTest {

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
    class StandardGeometryTests extends GeometryTests {

        @Override
        protected Geometry createSimpleGeometry() {
            return MultiLineString.from(
                LineString.from(Point.fromXY(0, 0), Point.fromXY(1, 1)),
                LineString.from(Point.fromXY(2, 2), Point.fromXY(3, 3))
            );
        }

        @Override
        protected Geometry createComplexGeometry() {
            return MultiLineString.from(
                LineString.from(Point.fromXY(0, 0), Point.fromXY(1, 1)),
                LineString.from(Point.fromXY(2, 2), Point.fromXY(3, 3)),
                LineString.from(Point.fromXY(4, 4), Point.fromXY(5, 5))
            );
        }

        @Test
        @Override
        public void toString_whenCalled_returnValidWkt() {
            assertEquals("MULTILINESTRING EMPTY", MultiLineString.EMPTY.toString());
            assertEquals("MULTILINESTRING ((1 2, 3 4))", MultiLineString.from(LineString.from(Point.fromXY(1, 2), Point.fromXY(3, 4))).toString());
            assertEquals("MULTILINESTRING ((1 2, 3 4), (5 6, 7 8))", MultiLineString.from(LineString.from(Point.fromXY(1, 2), Point.fromXY(3, 4)), LineString.from(Point.fromXY(5, 6), Point.fromXY(7, 8))).toString());
        }

        @Test
        @Override
        public void getPointCount_whenCalled_returnCorrectNumberOfPoints() {
            assertEquals(0, MultiLineString.EMPTY.getPointCount());
            assertEquals(2, MultiLineString.from(LineString.from(Point.fromXY(1, 2), Point.fromXY(3, 4))).getPointCount());
            assertEquals(4, MultiLineString.from(LineString.from(Point.fromXY(1, 2), Point.fromXY(3, 4)), LineString.from(Point.fromXY(5, 6), Point.fromXY(7, 8))).getPointCount());
        }
    }
}
