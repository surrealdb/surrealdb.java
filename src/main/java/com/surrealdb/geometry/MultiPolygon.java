package com.surrealdb.geometry;

import com.google.common.collect.ImmutableList;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

/**
 * MultiPolygons can be used to store multiple geometry polygons in a single value.
 *
 * @see <a href="https://surrealdb.com/docs/surrealql/datamodel/geometries#multipolygon">SurrealDB Docs - MultiPolygon</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7946#section-3.1.7">GeoJSON - MultiPolygon</a>
 */
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MultiPolygon extends GeometryPrimitive implements Iterable<Polygon> {

    public static final @NotNull MultiPolygon EMPTY = new MultiPolygon(ImmutableList.of());

    @NotNull ImmutableList<Polygon> polygons;

    /**
     * Creates a new MultiPolygon from the given polygons.
     *
     * @param polygons The polygons to store in this MultiPolygon
     * @return A new MultiPolygon with the given {@code polygons}
     * @throws NullPointerException If {@code polygons} contains a null value.
     */
    public static @NotNull MultiPolygon from(@NotNull Collection<Polygon> polygons) {
        if (polygons.isEmpty()) {
            return EMPTY;
        }

        return new MultiPolygon(ImmutableList.copyOf(polygons));
    }

    /**
     * Creates a new MultiPolygon from the given polygons.
     *
     * @param polygons The polygons to store in this MultiPolygon
     * @return A new MultiPolygon with the given {@code polygons}
     */
    public static @NotNull MultiPolygon from(Polygon @NotNull ... polygons) {
        if (polygons.length == 0) {
            return EMPTY;
        }

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

    public int getPolygonCount() {
        return polygons.size();
    }

    public @NotNull Polygon getPolygon(int index) {
        return polygons.get(index);
    }

    @NonNull
    @Override
    public Iterator<Polygon> iterator() {
        return polygons.iterator();
    }

    public @NotNull MultiPolygon translate(double x, double y) {
        return transform(polygon -> polygon.translate(x, y));
    }

    public @NotNull MultiPolygon rotate(double angle) {
        return transform(polygon -> polygon.rotate(angle));
    }

    public @NotNull MultiPolygon rotate(@NotNull Point center, double angle) {
        return transform(polygon -> polygon.rotate(center, angle));
    }

    public @NotNull MultiPolygon scale(double scaleX, double scaleY) {
        return transform(polygon -> polygon.scale(scaleX, scaleY));
    }

    public @NotNull MultiPolygon scale(@NotNull Point center, double scaleX, double scaleY) {
        return transform(polygon -> polygon.scale(center, scaleX, scaleY));
    }

    public @NotNull MultiPolygon transform(@NotNull Function<Polygon, Polygon> transformation) {
        if (this == EMPTY) {
            return EMPTY;
        }

        List<Polygon> transformed = new ArrayList<>(polygons.size());
        for (Polygon polygon : polygons) {
            transformed.add(transformation.apply(polygon));
        }

        return MultiPolygon.from(transformed);
    }

    @Override
    protected int calculatePointCount() {
        return InternalGeometryUtils.calculatePointCountOfGeometries(this);
    }

    @Override
    protected @NotNull String calculateWkt() {
        List<String> polygonWKTs = new ArrayList<>(polygons.size());

        for (Polygon polygon : polygons) {
            List<String> wktRings = new ArrayList<>(polygon.getInteriorCount() + 1);
            wktRings.add(InternalGeometryUtils.calculateWktPointsPrimitive(polygon.getExterior().iterator()));
            polygon.interiorIterator().forEachRemaining(interior -> wktRings.add(InternalGeometryUtils.calculateWktPointsPrimitive(interior.iterator())));

            polygonWKTs.add("(" + String.join(", ", wktRings) + ")");
        }

        return InternalGeometryUtils.calculateWktGeneric("MULTIPOLYGON", polygonWKTs);
    }

    @Override
    protected @NotNull Point calculateCenter() {
        return InternalGeometryUtils.calculateCenterOfGeometries(polygons);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {

        @NotNull List<Polygon> polygons = new ArrayList<>();

        /**
         * @param polygon The polygon to add
         * @return This {@code Builder} object
         */
        public @NotNull Builder addPolygon(@NotNull Polygon polygon) {
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
        public @NotNull Builder addPolygons(@NotNull Polygon... polygons) {
            Collections.addAll(this.polygons, polygons);
            return this;
        }

        /**
         * @param polygon The polygon to remove
         * @return This {@code Builder} object
         */
        public @NotNull Builder removePolygon(@NotNull Polygon polygon) {
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
