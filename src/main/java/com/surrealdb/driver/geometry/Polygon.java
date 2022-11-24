package com.surrealdb.driver.geometry;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

import static com.surrealdb.driver.geometry.InternalGeometryUtils.*;

/**
 * A GeoJSON Polygon value for storing a geometric area.
 *
 * @see <a href="https://surrealdb.com/docs/surrealql/datamodel/geometries#polygon">SurrealDB Docs - Polygon</a>
 * @see <a href="https://tools.ietf.org/html/rfc7946#section-3.1.6">GeoJSON Specification - Polygon</a>
 */
@EqualsAndHashCode(callSuper = false)
public final class Polygon extends GeometryPrimitive {

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

    public @NonNull LinearRing getExterior() {
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

    public @NotNull Polygon rotate(double degrees) {
        return transform(linearRing -> linearRing.rotate(degrees));
    }

    public @NotNull Polygon rotate(Point center, double degrees) {
        return transform(linearRing -> linearRing.rotate(center, degrees));
    }

    public @NotNull Polygon scale(double factor) {
        return transform(linearRing -> linearRing.scale(factor));
    }

    public @NotNull Polygon scale(Point center, double factor) {
        return transform(linearRing -> linearRing.scale(center, factor));
    }

    public @NotNull Polygon scale(double x, double y) {
        return transform(linearRing -> linearRing.scale(x, y));
    }

    public @NotNull Polygon scale(Point center, double x, double y) {
        return transform(linearRing -> linearRing.scale(center, x, y));
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
    protected int calculatePointCount() {
        int count = exterior.getPointCount();
        count += calculatePointCountOfGeometries(interiors);
        return count;
    }

    @Override
    protected @NotNull String calculateWkt() {
        List<String> wktRings = new ArrayList<>(interiors.size() + 1);
        wktRings.add(calculateWktPointsPrimitive(exterior.iterator(), true));
        for (LinearRing interior : interiors) {
            wktRings.add(calculateWktPointsPrimitive(interior.iterator(), true));
        }

        return calculateWktGeneric("POLYGON", wktRings);
    }

    @Override
    protected @NotNull Point calculateCenter() {
        List<LinearRing> rings = new ArrayList<>(interiors.size() + 1);
        rings.add(exterior);
        rings.addAll(interiors);

        return calculateCenterOfGeometries(rings);
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
}
