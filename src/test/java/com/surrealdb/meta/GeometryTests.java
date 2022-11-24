package com.surrealdb.meta;

import com.surrealdb.driver.geometry.Geometry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public abstract class GeometryTests {

    protected abstract Geometry createSimpleGeometry();

    protected abstract Geometry createComplexGeometry();

    @Test
    protected abstract void testToStringReturnsWKT();

    @Test
    protected abstract void testGetPointCountReturnsCorrectCount();

    @Test
    void testEqualsReturnsTrueForEqualObjects() {
        Geometry simpleGeo1 = createSimpleGeometry();
        Geometry simpleGeo2 = createSimpleGeometry();

        assertEquals(simpleGeo1, simpleGeo2, "Equal geometries should be equal - simple");

        Geometry complexGeo1 = createComplexGeometry();
        Geometry complexGeo2 = createComplexGeometry();

        assertEquals(complexGeo1, complexGeo2,"Equal geometries should be equal - complex");
    }

    @Test
    void testEqualsReturnsFalseForNull() {
        Geometry simpleGeo = createSimpleGeometry();
        assertNotEquals(simpleGeo, null, "Geometries should not be equal to null - simple");

        Geometry complexGeo = createComplexGeometry();
        assertNotEquals(complexGeo, null, "Geometries should not be equal to null - complex");
    }

    @Test
    void testEqualsReturnsFalseForDifferentGeometries() {
        Geometry simpleGeo = createSimpleGeometry();
        Geometry complexGeo = createComplexGeometry();

        assertNotEquals(simpleGeo, complexGeo, "Geometries should not be equal to different geometries - simple vs complex");
        assertNotEquals(complexGeo, simpleGeo, "Geometries should not be equal to different geometries - complex vs simple");
    }

    @Test
    void testHashCodesAreEqualForEqualObjects() {
        Geometry simpleGeo1 = createSimpleGeometry();
        Geometry simpleGeo2 = createSimpleGeometry();

        assertEquals(simpleGeo1.hashCode(), simpleGeo2.hashCode(), "Equal geometries should have equal hash codes - simple");

        Geometry complexGeo1 = createComplexGeometry();
        Geometry complexGeo2 = createComplexGeometry();

        assertEquals(complexGeo1.hashCode(), complexGeo2.hashCode(), "Equal geometries should have equal hash codes - complex");
    }

    @Test
    void testHashCodesAreNotEqualForDifferentObjects() {
        Geometry simpleGeo = createSimpleGeometry();
        Geometry complexGeo = createComplexGeometry();

        assertNotEquals(simpleGeo.hashCode(), complexGeo.hashCode(), "Different geometries should have different hash codes - simple vs complex");
        assertNotEquals(complexGeo.hashCode(), simpleGeo.hashCode(), "Different geometries should have different hash codes - complex vs simple");
    }

    @Test
    void testToStringReturnsCachedValue() {
        Geometry simpleGeo = createSimpleGeometry();
        assertSame(simpleGeo.toString(), simpleGeo.toString(), "toString should return the same value each time - simple");

        Geometry complexGeo = createComplexGeometry();
        assertSame(complexGeo.toString(), complexGeo.toString(), "toString should return the same value each time - complex");
    }
}
