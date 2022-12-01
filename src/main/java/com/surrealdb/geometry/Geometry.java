package com.surrealdb.geometry;

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

    public final int getPointCount() {
        if (pointCount == -1) {
            pointCount = calculatePointCount();
        }

        return pointCount;
    }

    protected abstract @NotNull Point calculateCenter();

    public final @NotNull Point getCenter() {
        if (center == null) {
            center = calculateCenter();
        }

        return center;
    }

    protected abstract @NotNull String calculateWkt();

    @Override
    public final @NotNull String toString() {
        if (wkt == null) {
            wkt = calculateWkt();
        }

        return wkt;
    }
}