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
 * A GeoJSON Polygon value for storing a geometric area.
 *
 * @author Damian Kocher
 * @see <a href="https://surrealdb.com/docs/surrealql/datamodel/geometries#polygon">SurrealDB Docs - Polygon</a>
 * @see <a href="https://tools.ietf.org/html/rfc7946#section-3.1.6">GeoJSON Specification - Polygon</a>
 */
@Value
public class Polygon implements GeometryPrimitive {

    Line exterior;
    ImmutableList<Line> interiors;

    // In the future this should probably validate that the exterior and interiors are valid
    // linear rings, but SurrealDB doesn't currently do that
    private Polygon(Line exterior, ImmutableList<Line> interiors) {
        this.exterior = exterior;
        this.interiors = interiors;
    }

    public static Polygon withInteriorPolygons(Line exterior, Collection<Line> interiors) {
        return new Polygon(exterior, ImmutableList.copyOf(interiors));
    }

    public static Polygon from(Line exterior) {
        return new Polygon(exterior, ImmutableList.of());
    }

    public static Builder builder() {
        return new Builder();
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {

        private Line exterior;
        private final List<Line> interiors = new ArrayList<>();

        public Builder setExterior(Line exterior) {
            this.exterior = exterior;
            return this;
        }

        public Builder addInterior(Line interior) {
            this.interiors.add(interior);
            return this;
        }

        public Builder addInteriors(Collection<Line> interiors) {
            this.interiors.addAll(interiors);
            return this;
        }

        public Builder addInteriors(Line... interiors) {
            Collections.addAll(this.interiors, interiors);
            return this;
        }

        public Builder removeInterior(Line interior) {
            this.interiors.remove(interior);
            return this;
        }

        public Builder removeInteriors(Collection<Line> interiors) {
            this.interiors.removeAll(interiors);
            return this;
        }

        public Builder removeInteriors(Line... interiors) {
            for (Line interior : interiors) {
                this.interiors.remove(interior);
            }
            return this;
        }

        public Polygon build() {
            return Polygon.withInteriorPolygons(exterior, interiors);
        }
    }
}
