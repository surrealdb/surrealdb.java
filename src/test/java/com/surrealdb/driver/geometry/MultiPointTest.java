package com.surrealdb.driver.geometry;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
