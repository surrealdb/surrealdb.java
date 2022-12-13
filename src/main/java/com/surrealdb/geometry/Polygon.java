package com.surrealdb.geometry;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * A GeoJSON Polygon value for storing a geometric area.
 *
 * @see <a href="https://surrealdb.com/docs/surrealql/datamodel/geometries#polygon">SurrealDB Docs - Polygon</a>
 * @see <a href="https://tools.ietf.org/html/rfc7946#section-3.1.6">GeoJSON Specification - Polygon</a>
 */
@EqualsAndHashCode(callSuper = false)
public final class Polygon extends Geometry {

    @NotNull LinearRing exterior;
    @NotNull ImmutableList<LinearRing> interiors;

    private Polygon(@NotNull LinearRing exterior, @NotNull ImmutableList<LinearRing> interiors) {
        this.exterior = exterior;
        this.interiors = interiors;
    }

    public static @NotNull Polygon withInteriorPolygons(@NotNull LinearRing exterior, @NotNull Collection<LinearRing> interiors) {
        return new Polygon(exterior, ImmutableList.copyOf(interiors));
    }

    public static @NotNull Polygon from(@NotNull LinearRing exterior) {
        return new Polygon(exterior, ImmutableList.of());
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public @NotNull LinearRing getExterior() {
        return exterior;
    }

    public int getInteriorCount() {
        return interiors.size();
    }

    public @NotNull LinearRing getInterior(int index) {
        return interiors.get(index);
    }

    public @NotNull Iterator<LinearRing> interiorIterator() {
        return interiors.iterator();
    }

    public @NotNull Polygon translate(double x, double y) {
        return transform(linearRing -> linearRing.translate(x, y));
    }

    public @NotNull Polygon rotate(@NotNull Point origin, double radians) {
        return transform(linearRing -> linearRing.rotate(origin, radians));
    }

    public @NotNull Polygon rotate(double radians) {
        return rotate(getCenter(), radians);
    }

    public @NotNull Polygon rotateDegrees(@NotNull Point origin, double degrees) {
        return transform(linearRing -> linearRing.rotateDegrees(origin, degrees));
    }

    public @NotNull Polygon rotateDegrees(double degrees) {
        return rotateDegrees(getCenter(), degrees);
    }

    public @NotNull Polygon scale(@NotNull Point origin, double factorX, double factorY) {
        return transform(linearRing -> linearRing.scale(origin, factorX, factorY));
    }

    public @NotNull Polygon scale(@NotNull Point origin, double factor) {
        return scale(origin, factor, factor);
    }

    public @NotNull Polygon scale(double x, double y) {
        return scale(getCenter(), x, y);
    }

    public @NotNull Polygon scale(double factor) {
        return scale(getCenter(), factor, factor);
    }

    public @NotNull Polygon transform(@NotNull Function<LinearRing, LinearRing> transformation) {
        LinearRing newExterior = transformation.apply(exterior);
        List<LinearRing> newInteriors = new ArrayList<>(interiors.size());
        for (LinearRing interior : interiors) {
            newInteriors.add(transformation.apply(interior));
        }

        return Polygon.withInteriorPolygons(newExterior, newInteriors);
    }

    @Override
    public @NotNull Iterator<Point> uniquePointsIterator() {
        return new PolygonPointIterator(this);
    }

    @Override
    protected int calculatePointCount() {
        int count = exterior.getPointCount();
        count += InternalGeometryUtils.calculatePointCountOfGeometries(interiors);
        return count;
    }

    @Override
    protected @NotNull String calculateWkt() {
        List<String> wktRings = new ArrayList<>(interiors.size() + 1);
        wktRings.add(InternalGeometryUtils.calculateWktPointsPrimitive(exterior.iterator()));
        for (LinearRing interior : interiors) {
            wktRings.add(InternalGeometryUtils.calculateWktPointsPrimitive(interior.iterator()));
        }

        return InternalGeometryUtils.calculateWktGeneric("POLYGON", wktRings);
    }

    @Override
    protected @NotNull Point calculateCenter() {
        return InternalGeometryUtils.calculateCenterOfGeometry(exterior);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {

        @NotNull List<LinearRing> interiors = new ArrayList<>();
        @NonFinal
        @Nullable LinearRing exterior;

        public @NotNull Builder setExterior(@NotNull LinearRing exterior) {
            this.exterior = exterior;
            return this;
        }

        public @NotNull Builder addInterior(@NotNull LinearRing interior) {
            this.interiors.add(interior);
            return this;
        }

        public @NotNull Builder addInteriors(@NotNull Collection<LinearRing> interiors) {
            this.interiors.addAll(interiors);
            return this;
        }

        public @NotNull Builder addInteriors(@NotNull LinearRing @NotNull ... interiors) {
            Collections.addAll(this.interiors, interiors);
            return this;
        }

        public @NotNull Builder removeInterior(@NotNull LinearRing interior) {
            this.interiors.remove(interior);
            return this;
        }

        public @NotNull Builder removeInteriors(@NotNull Collection<LinearRing> interiors) {
            for (LinearRing interior : interiors) {
                this.interiors.remove(interior);
            }
            return this;
        }

        public @NotNull Builder removeInteriors(@NotNull LinearRing @NotNull ... interiors) {
            for (LinearRing interior : interiors) {
                this.interiors.remove(interior);
            }
            return this;
        }

        public @NotNull Polygon build() {
            if (exterior == null) {
                throw new IllegalStateException("Exterior must be set");
            }

            return Polygon.withInteriorPolygons(exterior, interiors);
        }
    }

    private static class PolygonPointIterator implements Iterator<Point> {

        @NotNull Polygon polygon;

        @NonFinal
        int ringIndex = 0;
        @NonFinal
        int pointIndex = 0;

        private PolygonPointIterator(@NotNull Polygon polygon) {
            this.polygon = polygon;
        }

        @Override
        public boolean hasNext() {
            if (ringIndex >= (polygon.getInteriorCount() + 1)) {
                return false;
            }

            LinearRing ring = getRing(ringIndex);
            return ring.getPointCount() > pointIndex;
        }

        @Override
        public Point next() {
            LinearRing ring = getRing(ringIndex);
            Point point = ring.getPoint(pointIndex);

            pointIndex++;
            if (pointIndex >= ring.getPointCount() - 1) {
                ringIndex++;
                pointIndex = 0;
            }

            return point;
        }

        private @NotNull LinearRing getRing(int index) {
            if (index == 0) {
                return polygon.getExterior();
            }

            return polygon.getInterior(index - 1);
        }
    }
}
