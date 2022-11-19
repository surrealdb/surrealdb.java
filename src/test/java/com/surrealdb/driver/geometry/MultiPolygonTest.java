package com.surrealdb.driver.geometry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MultiPolygonTest implements MultiGeometryTest {

    @Override
    public void testEmptyConstantHasZeroElements() {
        assertEquals(0, MultiPolygon.EMPTY.getPolygonCount());
    }

    @Override
    public void testProvidingZeroElementsToFromGivesBackSingletonInstance() {
        assertSame(MultiPolygon.EMPTY, MultiPolygon.from());
        assertSame(MultiPolygon.EMPTY, MultiPolygon.from(List.of()));
    }
}
