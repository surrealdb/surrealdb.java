package com.surrealdb.meta;

import org.junit.jupiter.api.Test;

public interface GeometryTest {

    @Test
    void testToStringReturnsWKT();

    @Test
    void testToStringReturnsCachedString();

    @Test
    void testGetPointCountReturnsCorrectCount();

    @Test
    void testEqualsReturnsTrueForEqualObjects();

    @Test
    void testEqualsReturnsFalseForDifferentObjects();

    @Test
    void testHashCodeReturnsSameValueForEqualObjects();

}
