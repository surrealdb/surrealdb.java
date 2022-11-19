package com.surrealdb.driver.geometry;

import lombok.AccessLevel;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

public sealed abstract class Geometry permits GeometryPrimitive, GeometryCollection {

    @Getter(lazy = true, value = AccessLevel.PROTECTED)
    String wkt = calculateWkt();

    protected abstract @NotNull String calculateWkt();

    @Override
    public String toString() {
        return getWkt();
    }
}
