package com.surrealdb.driver.geometry;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Geometry Collections can be used to store multiple different geometry types in a single value. {@link Point}, {@link LineString},
 * {@link Polygon}, {@link MultiPoint}, {@link MultiLineString}, and {@link MultiPolygon} are supported. It's possible to store more
 * than one 'singe' geometry type, but it's not recommended. Instead, use {@code MultiPoint}, {@code MultiLine}, or {@code MultiPolygon}.
 *
 * @see <a href="https://surrealdb.com/docs/surrealql/datamodel/geometries#collection">SurrealDB Docs - Geometry Collections</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7946#section-3.1.8">GeoJSON - Geometry Collections</a>
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GeometryCollection {

    /**
     * A {@code GeometryCollection} without any geometries.
     */
    public static @NotNull GeometryCollection EMPTY = new GeometryCollection(ImmutableList.of());

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
            return addGeometries(geometryCollection.getGeometries());
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
            return removeGeometries(geometryCollection.getGeometries());
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
