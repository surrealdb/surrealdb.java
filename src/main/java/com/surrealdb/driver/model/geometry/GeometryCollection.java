package com.surrealdb.driver.model.geometry;

import com.google.common.collect.ImmutableList;
import lombok.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Geometry Collections can be used to store multiple different geometry types in a single value.
 *
 * @author Damian Kocher
 * @see <a href="https://surrealdb.com/docs/surrealql/datamodel/geometries#collection">SurrealDB Docs - Geometry Collections</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7946#section-3.1.8">GeoJSON - Geometry Collections</a>
 */
@Value
public class GeometryCollection {

    ImmutableList<GeometryPrimitive> geometries;

    private GeometryCollection(ImmutableList<GeometryPrimitive> geometries) {
        this.geometries = geometries;
    }

    /**
     * @param geometries The geometries to store in this collection.
     * @return A new GeometryCollection containing the given geometries.
     * @throws NullPointerException If the given geometries are null.
     */
    public static GeometryCollection from(Collection<GeometryPrimitive> geometries) {
        return new GeometryCollection(ImmutableList.copyOf(geometries));
    }

    public static GeometryCollection from(GeometryPrimitive... geometries) {
        return new GeometryCollection(ImmutableList.copyOf(geometries));
    }

    public static GeometryCollection from(GeometryPrimitive geometry) {
        return new GeometryCollection(ImmutableList.of(geometry));
    }

    public static class Builder {

        private final List<GeometryPrimitive> geometries = new ArrayList<>();

        public Builder add(GeometryPrimitive geometry) {
            geometries.add(geometry);
            return this;
        }

        public Builder add(Collection<GeometryPrimitive> geometries) {
            this.geometries.addAll(geometries);
            return this;
        }

        public Builder add(GeometryPrimitive... geometries) {
            Collections.addAll(this.geometries, geometries);
            return this;
        }

        public Builder add(GeometryCollection geometryCollection) {
            return add(geometryCollection.getGeometries());
        }

        public Builder remove(GeometryPrimitive geometry) {
            geometries.remove(geometry);
            return this;
        }

        public Builder remove(Collection<GeometryPrimitive> geometries) {
            this.geometries.removeAll(geometries);
            return this;
        }

        public Builder remove(GeometryPrimitive... geometries) {
            for (GeometryPrimitive geometry : geometries) {
                this.geometries.remove(geometry);
            }
            return this;
        }

        public Builder remove(GeometryCollection geometryCollection) {
            return remove(geometryCollection.getGeometries());
        }

        public Builder remove(int index) {
            geometries.remove(index);
            return this;
        }

        public GeometryCollection build() {
            return new GeometryCollection(ImmutableList.copyOf(geometries));
        }
    }

}
