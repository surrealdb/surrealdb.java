package com.surrealdb.driver.geometry;

import lombok.experimental.NonFinal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public sealed abstract class Geometry permits GeometryPrimitive, GeometryCollection {

    @NonFinal
    int pointCount = -1;

    @NonFinal
    @Nullable String wkt;

    @NonFinal
    @Nullable Point center;

    protected abstract int calculatePointCount();

    protected abstract @NotNull String calculateWkt();

    protected abstract @NotNull Point calculateCenter();

    public final int getPointCount() {
        if (pointCount == -1) {
            pointCount = calculatePointCount();
        }

        return pointCount;
    }

    public final @NotNull Point getCenter() {
        if (center == null) {
            center = calculateCenter();
        }

        return center;
    }

    @Override
    public final String toString() {
        if (wkt == null) {
            wkt = calculateWkt();
        }

        return wkt;
    }
}
