package com.surrealdb.driver.geometry;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MultiLineStringTest implements MultiGeometryTest {

    @Override
    public void testEmptyConstantHasZeroElements() {
        assertEquals(0, MultiLineString.EMPTY.getLineCount());
    }

    @Override
    public void testProvidingZeroElementsToFromGivesBackSingletonInstance() {
        assertSame(MultiLineString.EMPTY, MultiLineString.from());
        assertSame(MultiLineString.EMPTY, MultiLineString.from(List.of()));
        assertSame(MultiLineString.EMPTY, MultiLineString.builder().build());
    }
}
