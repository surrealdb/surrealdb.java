package com.surrealdb.driver.geometry;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * MultiPolygons can be used to store multiple geometry polygons in a single value.
 *
 * @author Damian Kocher
 * @see <a href="https://surrealdb.com/docs/surrealql/datamodel/geometries#multipolygon">SurrealDB Docs - MultiPolygon</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7946#section-3.1.7">GeoJSON - MultiPolygon</a>
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MultiPolygon implements GeometryPrimitive {

    @NotNull ImmutableList<Polygon> polygons;

    /**
     * Creates a new MultiPolygon from the given polygons.
     *
     * @param polygons The polygons to store in this MultiPolygon
     * @return A new MultiPolygon with the given {@code polygons}
     * @throws NullPointerException If {@code polygons} contains a null value.
     */
    public static @NotNull MultiPolygon from(@NotNull Collection<Polygon> polygons) {
        return new MultiPolygon(ImmutableList.copyOf(polygons));
    }

    /**
     * Creates a new MultiPolygon from the given polygons.
     *
     * @param polygons The polygons to store in this MultiPolygon
     * @return A new MultiPolygon with the given {@code polygons}
     */
    public static @NotNull MultiPolygon from(Polygon @NotNull ... polygons) {
        return new MultiPolygon(ImmutableList.copyOf(polygons));
    }

    /**
     * @param polygon The polygon to store in this MultiPolygon
     * @return A new MultiPolygon containing just the given {@code polygon}
     * @throws NullPointerException If {@code polygon} is null.
     */
    public static @NotNull MultiPolygon from(@NotNull Polygon polygon) {
        return new MultiPolygon(ImmutableList.of(polygon));
    }

    /**
     * @return A new {@link MultiPolygon.Builder} instance
     */
    public static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new {@link MultiPolygon.Builder} with all polygons from this {@code MultiPolygon}.
     *
     * @return A new {@link MultiPolygon.Builder} instance with the polygons of this {@code MultiPolygon}.
     */
    public @NotNull Builder toBuilder() {
        return builder().addPolygons(polygons);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static class Builder {

        private final List<Polygon> polygons = new ArrayList<>();

        /**
         * @param polygon The polygon to add
         * @return This {@code Builder} object
         */
        public @NotNull Builder addPolygon(Polygon polygon) {
            polygons.add(polygon);
            return this;
        }

        /**
         * @param polygons The polygons to add
         * @return This {@code Builder} object
         */
        public @NotNull Builder addPolygons(@NotNull Collection<Polygon> polygons) {
            this.polygons.addAll(polygons);
            return this;
        }

        /**
         * @param polygons The polygons to add
         * @return This {@code Builder} object
         */
        public @NotNull Builder addPolygons(Polygon... polygons) {
            Collections.addAll(this.polygons, polygons);
            return this;
        }

        /**
         * @param polygon The polygon to remove
         * @return This {@code Builder} object
         */
        public @NotNull Builder removePolygon(Polygon polygon) {
            polygons.remove(polygon);
            return this;
        }

        /**
         * @param polygons The polygons to remove
         * @return This {@code Builder} object
         */
        public @NotNull Builder removePolygons(@NotNull Collection<Polygon> polygons) {
            this.polygons.removeAll(polygons);
            return this;
        }

        /**
         * @param polygons The polygons to remove
         * @return This {@code Builder} object
         */
        public @NotNull Builder removePolygons(Polygon @NotNull ... polygons) {
            for (Polygon polygon : polygons) {
                this.polygons.remove(polygon);
            }
            return this;
        }

        /**
         * @return A new MultiPolygon with the polygons added to this {@code Builder}
         */
        public @NotNull MultiPolygon build() {
            return MultiPolygon.from(polygons);
        }
    }
}
