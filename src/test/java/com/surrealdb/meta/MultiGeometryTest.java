package com.surrealdb.meta;

import org.junit.jupiter.api.Test;

public interface MultiGeometryTest {

    @Test
    void testEmptyConstantHasZeroElements();

    @Test
    void testProvidingZeroElementsToFromGivesBackSingletonInstance();
}
