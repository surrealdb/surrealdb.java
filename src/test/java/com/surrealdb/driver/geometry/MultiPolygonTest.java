package com.surrealdb.driver.geometry;

import com.surrealdb.meta.GeometryTest;
import com.surrealdb.meta.MultiGeometryTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MultiPolygonTest implements GeometryTest, MultiGeometryTest {

    @Override
    public void testEmptyConstantHasZeroElements() {
        assertEquals(0, MultiPolygon.EMPTY.getPolygonCount());
    }

    @Override
    public void testProvidingZeroElementsToFromGivesBackSingletonInstance() {
        assertSame(MultiPolygon.EMPTY, MultiPolygon.from());
        assertSame(MultiPolygon.EMPTY, MultiPolygon.from(List.of()));
    }

    @Override
    public void testToStringReturnsWKT() {

    }

    @Override
    public void testEqualsReturnsTrueForEqualObjects() {

    }

    @Override
    public void testEqualsReturnsFalseForDifferentObjects() {

    }

    @Override
    public void testHashCodeReturnsSameValueForEqualObjects() {

    }
}
