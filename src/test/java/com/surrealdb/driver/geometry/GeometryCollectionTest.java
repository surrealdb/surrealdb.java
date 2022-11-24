package com.surrealdb.driver.geometry;

import com.surrealdb.meta.GeometryTest;
import com.surrealdb.meta.MultiGeometryTest;
import com.surrealdb.meta.utils.GeometryUtils;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeometryCollectionTest {

    @Test
    void testGetGeometryCount() {
        GeometryCollection collection = GeometryCollection.from(
            Point.fromXY(0, 0),
            LineString.from(
                Point.fromXY(5, 12),
                Point.fromXY(8, 3)
            ),
            GeometryUtils.createCirclePolygon(12, 4)
        );

        assertEquals(3, collection.getGeometryCount());
    }

    @Test
    void testGetGeometry() {
        MultiPoint multiPoint = MultiPoint.from(
            Point.fromXY(2, 3),
            Point.fromXY(4, 5)
        );
        LinearRing ring = GeometryUtils.createQuadLinearRing(true);

        GeometryCollection collection = GeometryCollection.builder()
            .addGeometry(multiPoint)
            .addGeometry(ring)
            .build();

        assertSame(multiPoint, collection.getGeometry(0));
        assertSame(ring, collection.getGeometry(1));
    }

    @Nested
    class MultiGeometryTests implements MultiGeometryTest {

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
    class StandardGeometryTests implements GeometryTest {

        @Test
        public void testToStringReturnsWKT() {
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

        @Override
        public void testGetPointCountReturnsCorrectCount() {
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
        public void testEqualsReturnsTrueForEqualObjects() {
            GeometryCollection collection1 = GeometryCollection.from(
                Point.fromXY(0, 0),
                MultiLineString.from(
                    LineString.from(
                        Point.fromXY(5, 12),
                        Point.fromXY(8, 3)
                    ),
                    LineString.from(
                        Point.fromXY(2, 3),
                        Point.fromXY(4, 5)
                    )
                )
            );
            GeometryCollection collection2 = GeometryCollection.from(
                Point.fromXY(0, 0),
                MultiLineString.from(
                    LineString.from(
                        Point.fromXY(5, 12),
                        Point.fromXY(8, 3)
                    ),
                    LineString.from(
                        Point.fromXY(2, 3),
                        Point.fromXY(4, 5)
                    )
                )
            );

            assertEquals(collection1, collection2);
        }

        @Test
        public void testEqualsReturnsFalseForDifferentObjects() {
            GeometryCollection collection1 = GeometryCollection.from(
                LineString.from(
                    Point.fromXY(13, -1),
                    Point.fromXY(3, 51)
                ),
                Point.fromXY(0, 0)
            );
            GeometryCollection collection2 = GeometryCollection.from(
                LineString.from(
                    Point.fromXY(5, 12),
                    Point.fromXY(8, 3)
                ),
                MultiPoint.from(
                    Point.fromXY(2, 3),
                    Point.fromXY(4, 5)
                )
            );

            assertNotEquals(collection1, collection2);
        }

        @Test
        public void testHashCodeReturnsSameValueForEqualObjects() {
            GeometryCollection collection1 = GeometryCollection.from(
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
            GeometryCollection collection2 = GeometryCollection.from(
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

            assertEquals(collection1.hashCode(), collection2.hashCode());
        }
    }
}
