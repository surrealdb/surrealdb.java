package com.surrealdb.driver.geometry;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A GeoJSON Polygon value for storing a geometric area.
 *
 * @author Damian Kocher
 * @see <a href="https://surrealdb.com/docs/surrealql/datamodel/geometries#polygon">SurrealDB Docs - Polygon</a>
 * @see <a href="https://tools.ietf.org/html/rfc7946#section-3.1.6">GeoJSON Specification - Polygon</a>
 */
@Value
public class Polygon implements GeometryPrimitive {

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
