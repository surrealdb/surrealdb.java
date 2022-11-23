package com.surrealdb.driver.geometry;

import com.surrealdb.meta.GeometryTest;
import com.surrealdb.meta.MultiGeometryTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class MultiPointTest implements MultiGeometryTest {

    @Test
    public void testEmptyConstantHasZeroElements() {
        assertEquals(0, MultiPoint.EMPTY.getPointCount());
    }

    @Test
    public void testProvidingZeroElementsToFromGivesBackSingletonInstance() {
        assertEquals(MultiPoint.EMPTY, MultiPoint.from());
        assertEquals(MultiPoint.EMPTY, MultiPoint.from(List.of()));
    }

    @Nested
    class StandardGeometryTests implements GeometryTest {

        @Test
        @Override
        public void testToStringReturnsWKT() {
            MultiPoint multiPoint = MultiPoint.from(
                Point.fromXY(-91.6711, -13.4225),
                Point.fromXY(-68.295, 7.24),
                Point.fromXY(3.9, 7)
            );

            assertEquals("MULTIPOINT (-91.6711 -13.4225, -68.295 7.24, 3.9 7)", multiPoint.toString());
        }

        @Test
        @Override
        public void testEqualsReturnsTrueForEqualObjects() {
            MultiPoint multiPoint1 = MultiPoint.from(
                Point.fromXY(-91.6711, -13.4225),
                Point.fromXY(-68.295, 7.24),
                Point.fromXY(3.9, 7)
            );
            MultiPoint multiPoint2 = MultiPoint.from(
                Point.fromXY(-91.6711, -13.4225),
                Point.fromXY(-68.295, 7.24),
                Point.fromXY(3.9, 7)
            );

            assertEquals(multiPoint1, multiPoint2);
        }

        @Test
        @Override
        public void testEqualsReturnsFalseForDifferentObjects() {
            MultiPoint multiPoint1 = MultiPoint.from(
                Point.fromXY(-91.6711, -13.4225),
                Point.fromXY(-68.295, 7.24),
                Point.fromXY(3.9, 7)
            );
            MultiPoint multiPoint2 = MultiPoint.from(
                Point.fromXY(-91.6711, -13.4225),
                Point.fromXY(-68.295, 7.24)
            );

            assertNotEquals(multiPoint1, multiPoint2);
        }

        @Test
        @Override
        public void testHashCodeReturnsSameValueForEqualObjects() {
            MultiPoint multiPoint1 = MultiPoint.from(
                Point.fromXY(-91.6711, -13.4225),
                Point.fromXY(-68.295, 7.24),
                Point.fromXY(3.9, 7)
            );
            MultiPoint multiPoint2 = MultiPoint.from(
                Point.fromXY(-91.6711, -13.4225),
                Point.fromXY(-68.295, 7.24),
                Point.fromXY(3.9, 7)
            );

            assertEquals(multiPoint1.hashCode(), multiPoint2.hashCode());
        }
    }
}
