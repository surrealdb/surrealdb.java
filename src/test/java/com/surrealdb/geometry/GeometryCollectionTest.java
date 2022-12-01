package com.surrealdb.geometry;

import meta.tests.GeometryTests;
import meta.tests.MultiGeometryTest;
import meta.utils.GeometryUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static meta.utils.GeometryUtils.createCirclePolygon;
import static meta.utils.GeometryUtils.createQuadPolygon;
import static org.junit.jupiter.api.Assertions.*;

class GeometryCollectionTest {

    @Test
    void getGeometryCount_whenCalled_returnsCorrectNumberOfGeometries() {
        GeometryCollection collection = GeometryCollection.from(
            Point.fromXY(0, 0),
            LineString.from(
                Point.fromXY(5, 12),
                Point.fromXY(8, 3)
            ),
            createCirclePolygon(12, 4)
        );

        assertEquals(3, collection.getGeometryCount());
    }

    @Test
    void getGeometry_whenGivenIndexWithinBounds_returnsGeometryAtIndex() {
        MultiPoint multiPoint = MultiPoint.from(
            Point.fromXY(2, 3),
            Point.fromXY(4, 5)
        );
        LinearRing ring = GeometryUtils.createQuadLinearRing(true, true);

        GeometryCollection collection = GeometryCollection.builder()
            .addGeometry(multiPoint)
            .addGeometry(ring)
            .build();

        assertSame(multiPoint, collection.getGeometry(0));
        assertSame(ring, collection.getGeometry(1));
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    void getGeometry_whenGivenIndexOutsideOfBounds_throwsException() {
        GeometryCollection collection = GeometryCollection.builder()
            .addGeometry(GeometryUtils.createQuadPolygon(true))
            .build();

        assertThrows(IndexOutOfBoundsException.class, () -> collection.getGeometry(1));
        assertThrows(IndexOutOfBoundsException.class, () -> collection.getGeometry(2));
    }

    @Nested
    class MultiGeometryTests extends MultiGeometryTest {

        @Test
        public void testEmptyConstantHasZeroElements() {
            assertEquals(0, GeometryCollection.EMPTY.getGeometryCount());
        }

        @Test
        public void testProvidingZeroElementsToFromGivesBackSingletonInstance() {
            assertSame(GeometryCollection.EMPTY, GeometryCollection.from());
            assertSame(GeometryCollection.EMPTY, GeometryCollection.from(List.of()));
            assertSame(GeometryCollection.EMPTY, GeometryCollection.builder().build());
        }
    }

    @Nested
    class StandardGeometryTests extends GeometryTests {

        @Override
        protected Geometry createSimpleGeometry() {
            return GeometryCollection.from(
                Point.fromXY(5, 12),
                LineString.from(
                    Point.fromXY(5, 12),
                    Point.fromXY(8, 3)
                )
            );
        }

        @Override
        protected Geometry createComplexGeometry() {
            return GeometryCollection.from(
                Point.fromXY(5, 12),
                LineString.from(
                    Point.fromXY(5, 12),
                    Point.fromXY(8, 3)
                ),
                MultiPolygon.from(
                    createCirclePolygon(12, 4),
                    createCirclePolygon(30, 12).translate(10, 10),
                    createQuadPolygon(false)
                )
            );
        }

        @Test
        @Override
        protected void getPointCount_whenCalled_returnCorrectNumberOfPoints() {
            GeometryCollection collection = GeometryCollection.from(
                Point.fromXY(0, 0),
                LineString.from(
                    Point.fromXY(5, 12),
                    Point.fromXY(8, 3)
                ),
                MultiPoint.from(
                    Point.fromXY(2, 3),
                    Point.fromXY(4, 5)
                )
            );

            assertEquals(5, collection.getPointCount());
        }

        @Test
        @Override
        protected void toString_whenCalled_returnValidWkt() {
            GeometryCollection collection = GeometryCollection.from(
                Point.fromXY(0, 0),
                LineString.from(
                    Point.fromXY(5, 12),
                    Point.fromXY(8, 3)
                ),
                MultiPoint.from(
                    Point.fromXY(2, 3),
                    Point.fromXY(4, 5)
                )
            );

            assertEquals("GEOMETRYCOLLECTION (POINT (0 0), LINESTRING (5 12, 8 3), MULTIPOINT (2 3, 4 5))", collection.toString());
            assertEquals("GEOMETRYCOLLECTION EMPTY", GeometryCollection.EMPTY.toString());
        }
    }
}
