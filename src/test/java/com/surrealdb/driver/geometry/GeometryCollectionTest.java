package com.surrealdb.driver.geometry;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class GeometryCollectionTest {

    @Test
    void testEmpty() {
        assertEquals(0, GeometryCollection.EMPTY.getGeometryCount());
    }

    @Test
    void testEmptyCreateReturnsSameInstance() {
        assertSame(GeometryCollection.EMPTY, GeometryCollection.from());
        assertSame(GeometryCollection.EMPTY, GeometryCollection.from(new ArrayList<>()));
        assertSame(GeometryCollection.EMPTY, GeometryCollection.builder().build());
    }

    @Test
    void testGetGeometryCount() {
        GeometryCollection collection = GeometryCollection.from(
            Point.fromXY(0, 0),
            LineString.from(
                Point.fromXY(5, 12),
                Point.fromXY(8, 3)
            ),
            Polygon.from(
                LinearRing.from(
                    Point.fromXY(0, 0),
                    Point.fromXY(1, 0),
                    Point.fromXY(1, 1),
                    Point.fromXY(0, 1)
                )
            )
        );

        assertEquals(3, collection.getGeometryCount());
    }

    @Test
    void testGetGeometry() {
        MultiPoint multiPoint = MultiPoint.from(
            Point.fromXY(2, 3),
            Point.fromXY(4, 5)
        );
        LinearRing ring = LinearRing.from(
            Point.fromXY(0, 0),
            Point.fromXY(1, 0),
            Point.fromXY(1, 1),
            Point.fromXY(0, 1)
        );

        GeometryCollection collection = GeometryCollection.from(
            multiPoint,
            ring
        );

        assertSame(multiPoint, collection.getGeometry(0));
        assertSame(ring, collection.getGeometry(1));
    }
}
