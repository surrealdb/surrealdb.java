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
import java.util.Collections;
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

    @NotNull Line exterior;
    @NotNull ImmutableList<Line> interiors;

    // In the future this should probably validate that the exterior and interiors are valid
    // linear rings, but SurrealDB doesn't currently do that
    private Polygon(@NotNull Line exterior, @NotNull ImmutableList<Line> interiors) {
        this.exterior = exterior;
        this.interiors = interiors;
    }

    public static @NotNull Polygon withInteriorPolygons(@NotNull Line exterior, @NotNull Collection<Line> interiors) {
        return new Polygon(exterior, ImmutableList.copyOf(interiors));
    }

    public static @NotNull Polygon from(@NotNull Line exterior) {
        return new Polygon(exterior, ImmutableList.of());
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {

        @NotNull List<Line> interiors = new ArrayList<>();
        @NonFinal
        @Nullable Line exterior;

        public @NotNull Builder setExterior(@NotNull Line exterior) {
            this.exterior = exterior;
            return this;
        }

        public @NotNull Builder addInterior(Line interior) {
            this.interiors.add(interior);
            return this;
        }

        public @NotNull Builder addInteriors(@NotNull Collection<Line> interiors) {
            this.interiors.addAll(interiors);
            return this;
        }

        public @NotNull Builder addInteriors(@NotNull Line... interiors) {
            Collections.addAll(this.interiors, interiors);
            return this;
        }

        public @NotNull Builder removeInterior(@NotNull Line interior) {
            this.interiors.remove(interior);
            return this;
        }

        public @NotNull Builder removeInteriors(@NotNull Collection<Line> interiors) {
            this.interiors.removeAll(interiors);
            return this;
        }

        public @NotNull Builder removeInteriors(@NotNull Line... interiors) {
            for (Line interior : interiors) {
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
