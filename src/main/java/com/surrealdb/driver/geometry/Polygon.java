package com.surrealdb.driver.geometry;

import com.google.common.collect.ImmutableList;
import lombok.*;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static com.surrealdb.driver.geometry.InternalGeometryUtils.calculateWktGeneric;
import static com.surrealdb.driver.geometry.InternalGeometryUtils.calculateWktPointsPrimitive;

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

    @Override
    protected @NotNull String calculateWkt() {
        List<String> wktRings = new ArrayList<>(interiors.size() + 1);
        wktRings.add(calculateWktPointsPrimitive(exterior.iterator(), true));
        for (LinearRing interior : interiors) {
            wktRings.add(calculateWktPointsPrimitive(interior.iterator(), true));
        }

        return calculateWktGeneric("POLYGON", wktRings);
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
