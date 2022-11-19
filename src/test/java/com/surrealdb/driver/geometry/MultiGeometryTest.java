package com.surrealdb.driver.geometry;

import org.junit.jupiter.api.Test;

public interface MultiGeometryTest {

    @Test
    void testEmptyConstantHasZeroElements();

    @Test
    void testProvidingZeroElementsToFromGivesBackSingletonInstance();
}
