package com.surrealdb.driver.geometry;

import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public sealed abstract class Geometry permits GeometryPrimitive, GeometryCollection {

    @Getter(lazy = true, value = AccessLevel.PUBLIC)
    int pointCount = calculatePointCount();

    @Getter(lazy = true, value = AccessLevel.PROTECTED)
    @NotNull String wkt = calculateWkt();

    @Getter(lazy = true, value = AccessLevel.PUBLIC)
    @NotNull Point center = calculateCenter();

    protected abstract int calculatePointCount();

    protected abstract @NotNull String calculateWkt();

    protected abstract @NotNull Point calculateCenter();

    @Override
    public String toString() {
        return getWkt();
    }
}
