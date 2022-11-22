package com.surrealdb.driver.geometry;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
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
        return transform(linearRing -> linearRing.translate(x, y).toLinearRing());
    }

    public @NotNull Polygon rotate(double degrees) {
        return transform(linearRing -> linearRing.rotate(degrees).toLinearRing());
    }

    public @NotNull Polygon rotate(Point center, double degrees) {
        return transform(linearRing -> linearRing.rotate(center, degrees).toLinearRing());
    }

    public @NotNull Polygon scale(double factor) {
        return transform(linearRing -> linearRing.scale(factor).toLinearRing());
    }

    public @NotNull Polygon scale(Point center, double factor) {
        return transform(linearRing -> linearRing.scale(center, factor).toLinearRing());
    }

    public @NotNull Polygon scale(double x, double y) {
        return transform(linearRing -> linearRing.scale(x, y).toLinearRing());
    }

    public @NotNull Polygon scale(Point center, double x, double y) {
        return transform(linearRing -> linearRing.scale(center, x, y).toLinearRing());
    }

    public @NotNull Polygon transform(@NotNull Function<LinearRing, LinearRing> transformation) {
        LinearRing newExterior = transformation.apply(exterior);
        ImmutableList<LinearRing> newInteriors = interiors.stream().map(transformation).collect(ImmutableList.toImmutableList());

        return new Polygon(newExterior, newInteriors);
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

        public @NotNull Builder setExterior(@NotNull LineString exterior) {
            this.exterior = exterior.toLinearRing();
            return this;
        }

        public @NotNull Builder addInterior(@NotNull LineString interior) {
            this.interiors.add(interior.toLinearRing());
            return this;
        }

        public @NotNull Builder addInteriors(@NotNull Collection<LineString> interiors) {
            for (LineString interior : interiors) {
                this.interiors.add(interior.toLinearRing());
            }
            return this;
        }

        public @NotNull Builder addInteriors(@NotNull LineString @NotNull ... interiors) {
            for (LineString interior : interiors) {
                this.interiors.add(interior.toLinearRing());
            }
            return this;
        }

        public @NotNull Builder removeInterior(@NotNull LineString interior) {
            this.interiors.remove(interior.toLinearRing());
            return this;
        }

        public @NotNull Builder removeInteriors(@NotNull Collection<LineString> interiors) {
            for (LineString interior : interiors) {
                this.interiors.remove(interior.toLinearRing());
            }
            return this;
        }

        public @NotNull Builder removeInteriors(@NotNull LineString @NotNull ... interiors) {
            for (LineString interior : interiors) {
                this.interiors.remove(interior.toLinearRing());
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
