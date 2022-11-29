package meta.tests;

import org.junit.jupiter.api.Test;

public abstract class MultiGeometryTest {

    @Test
    public abstract void testEmptyConstantHasZeroElements();

    @Test
    public abstract void testProvidingZeroElementsToFromGivesBackSingletonInstance();
}
