package com.surrealdb.driver.model.geometry;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

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
public class MultiPolygon implements GeometryPrimitive {

    ImmutableList<Polygon> polygons;

    /**
     * @param polygons The polygons to store in this MultiPolygon.
     * @throws NullPointerException If {@code polygons} contains a null value.
     */
    private MultiPolygon(ImmutableList<Polygon> polygons) {
        this.polygons = polygons;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return builder().addPolygons(polygons);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static class Builder {

        private final List<Polygon> polygons = new ArrayList<>();

        public Builder addPolygon(Polygon polygon) {
            polygons.add(polygon);
            return this;
        }

        public Builder addPolygons(Collection<Polygon> polygons) {
            this.polygons.addAll(polygons);
            return this;
        }

        public Builder addPolygons(Polygon... polygons) {
            Collections.addAll(this.polygons, polygons);
            return this;
        }

        public Builder removePolygon(Polygon polygon) {
            polygons.remove(polygon);
            return this;
        }

        public Builder removePolygons(Collection<Polygon> polygons) {
            this.polygons.removeAll(polygons);
            return this;
        }

        public Builder removePolygons(Polygon... polygons) {
            for (Polygon polygon : polygons) {
                this.polygons.remove(polygon);
            }
            return this;
        }

        public MultiPolygon build() {
            return new MultiPolygon(ImmutableList.copyOf(polygons));
        }
    }

}
