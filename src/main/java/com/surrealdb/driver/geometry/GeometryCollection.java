package com.surrealdb.driver.geometry;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.surrealdb.driver.geometry.InternalGeometryUtils.calculateCenterOfGeometries;

/**
 * Geometry Collections can be used to store multiple different geometry types in a single value. {@link Point}, {@link LineString},
 * {@link Polygon}, {@link MultiPoint}, {@link MultiLineString}, and {@link MultiPolygon} are supported. It's possible to store more
 * than one 'singe' geometry type, but it's not recommended. Instead, use {@code MultiPoint}, {@code MultiLine}, or {@code MultiPolygon}.
 *
 * @see <a href="https://surrealdb.com/docs/surrealql/datamodel/geometries#collection">SurrealDB Docs - Geometry Collections</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7946#section-3.1.8">GeoJSON - Geometry Collections</a>
 */
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class GeometryCollection extends Geometry implements Iterable<GeometryPrimitive> {

    /**
     * A {@code GeometryCollection} without any geometries.
     */
    public static final @NotNull GeometryCollection EMPTY = new GeometryCollection(ImmutableList.of());

    @NotNull ImmutableList<GeometryPrimitive> geometries;

    /**
     * Creates and returns a new {@code GeometryCollection} with the given geometries.
     * Elements from the provided {@code Collection} are copied, meaning that changes to the provided
     * {@code Collection} will not be reflected in the returned {@code GeometryCollection}.
     *
     * @param geometries The geometries to store in this collection.
     * @return A new GeometryCollection containing the provided geometries
     * @throws NullPointerException If the provided {@code geometries} contains a null element
     * @see GeometryCollection#from(GeometryPrimitive...)
     * @see GeometryCollection#from(GeometryPrimitive)
     */
    public static @NotNull GeometryCollection from(@NotNull Collection<GeometryPrimitive> geometries) {
        if (geometries.isEmpty()) {
            return EMPTY;
        }

        return new GeometryCollection(ImmutableList.copyOf(geometries));
    }

    /**
     * Creates and returns a new {@code GeometryCollection} with the given geometries.
     * Elements from the provided {@code array} are copied, meaning that changes to the provided
     * {@code array} will not be reflected in the returned {@code GeometryCollection}.
     *
     * @param geometries The geometries to store in this collection.
     * @return A new GeometryCollection containing the provided geometries
     * @throws NullPointerException If the provided {@code geometries} contains a null element
     * @see GeometryCollection#from(Collection)
     * @see GeometryCollection#from(GeometryPrimitive)
     */
    public static @NotNull GeometryCollection from(GeometryPrimitive @NotNull ... geometries) {
        if (geometries.length == 0) {
            return EMPTY;
        }

        return new GeometryCollection(ImmutableList.copyOf(geometries));
    }

    /**
     * Creates and returns a new {@code GeometryCollection} with the given geometry.
     *
     * @param geometry The geometry to store in this collection.
     * @return A new GeometryCollection containing the provided geometry
     * @see GeometryCollection#from(Collection)
     * @see GeometryCollection#from(GeometryPrimitive...)
     */
    public static @NotNull GeometryCollection from(@NotNull GeometryPrimitive geometry) {
        return new GeometryCollection(ImmutableList.of(geometry));
    }

    public static @NotNull Builder builder() {
        return new Builder();
    }

    public @NotNull Builder toBuilder() {
        return new Builder().addGeometries(this);
    }

    public @NotNull GeometryPrimitive getGeometry(int index) {
        return geometries.get(index);
    }

    public int getGeometryCount() {
        return geometries.size();
    }

    @Override
    public @NotNull Iterator<GeometryPrimitive> iterator() {
        return geometries.iterator();
    }

    @Override
    public int calculatePointCount() {
        return InternalGeometryUtils.calculatePointCountOfGeometries(this);
    }

    @Override
    protected @NotNull String calculateWkt() {
        // Since the returned string is cached, the overhead of using a stream
        // is negligible.
        List<String> wktGeometries = geometries.stream()
            .map(Geometry::getWkt)
            .toList();

        return InternalGeometryUtils.calculateWktGeneric("GEOMETRYCOLLECTION", wktGeometries);
    }

    @Override
    protected @NotNull Point calculateCenter() {
        return calculateCenterOfGeometries(geometries);
    }

    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {

        @NotNull List<GeometryPrimitive> geometries = new ArrayList<>();

        /**
         * @param geometry The geometry to add
         * @return This {@code Builder} object
         */
        public @NotNull Builder addGeometry(GeometryPrimitive geometry) {
            geometries.add(geometry);
            return this;
        }

        /**
         * @param geometries The geometries to add
         * @return This {@code Builder} object
         */
        public @NotNull Builder addGeometries(@NotNull Collection<GeometryPrimitive> geometries) {
            this.geometries.addAll(geometries);
            return this;
        }

        /**
         * @param geometries The geometries to add
         * @return This {@code Builder} object
         */
        public @NotNull Builder addGeometries(GeometryPrimitive... geometries) {
            Collections.addAll(this.geometries, geometries);
            return this;
        }

        /**
         * Copies the geometries from the provided {@code GeometryCollection} into this {@code Builder}.
         *
         * @param geometryCollection The {@code GeometryCollection} to add
         * @return This {@code Builder} object
         */
        public @NotNull Builder addGeometries(@NotNull GeometryCollection geometryCollection) {
            geometries.addAll(geometryCollection.geometries);
            return this;
        }

        /**
         * @param geometry The geometry to remove
         * @return This {@code Builder} object
         */
        public @NotNull Builder removeGeometry(GeometryPrimitive geometry) {
            geometries.remove(geometry);
            return this;
        }

        /**
         * @param geometries The geometries to remove
         * @return This {@code Builder} object
         */
        public @NotNull Builder removeGeometries(@NotNull Collection<GeometryPrimitive> geometries) {
            this.geometries.removeAll(geometries);
            return this;
        }

        /**
         * @param geometries The geometries to remove
         * @return This {@code Builder} object
         */
        public @NotNull Builder removeGeometries(GeometryPrimitive @NotNull ... geometries) {
            for (GeometryPrimitive geometry : geometries) {
                this.geometries.remove(geometry);
            }
            return this;
        }

        /**
         * Removes the geometries from the provided {@code GeometryCollection} from this {@code Builder}.
         *
         * @param geometryCollection The {@code GeometryCollection} to remove
         * @return This {@code Builder} object
         */
        public @NotNull Builder removeGeometries(@NotNull GeometryCollection geometryCollection) {
            return removeGeometries(geometryCollection.geometries);
        }

        /**
         * Creates and returns a new {@code GeometryCollection} with the geometries added to this {@code Builder}.
         * This Builder's backing list is copied, meaning that this Builder can be reused after calling this method.
         *
         * @return A new {@code GeometryCollection} with the geometries added to this {@code Builder}
         */
        public @NotNull GeometryCollection build() {
            return GeometryCollection.from(geometries);
        }
    }
}
