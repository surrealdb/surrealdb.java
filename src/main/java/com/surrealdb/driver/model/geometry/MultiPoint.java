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
 * MultiPoints can be used to store multiple geometry points in a single value.
 *
 * @author Damian Kocher
 * @see <a href="https://surrealdb.com/docs/surrealql/datamodel/geometries#multipoint">Surreal Docs - MultiPoint</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7946#section-3.1.3">GeoJSON - MultiPoint</a>
 */
@Value
public class MultiPoint implements GeometryPrimitive {

    ImmutableList<Point> points;

    private MultiPoint(ImmutableList<Point> points) {
        this.points = points;
    }

    /**
     * @param points The points to store in this MultiPoint.
     * @return A new MultiPoint with the given points.
     * @throws NullPointerException If {@code points} contains a null value.
     */
    public static MultiPoint fromPoints(Collection<Point> points) {
        return new MultiPoint(ImmutableList.copyOf(points));
    }

    /**
     * @param points The points to store in this MultiPoint.
     * @return A new MultiPoint with the given points.
     * @throws NullPointerException If {@code points} contains a null value.
     */
    public static MultiPoint fromPoints(Point... points) {
        return new MultiPoint(ImmutableList.copyOf(points));
    }

    public static Builder builder() {
        return new Builder();
    }

    public Builder toBuilder() {
        return builder().addPoints(points);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static class Builder {

        private final List<Point> points = new ArrayList<>();

        Builder(MultiPoint multiPoint) {
            points.addAll(multiPoint.getPoints());
        }

        public Builder addPoint(Point point) {
            points.add(point);
            return this;
        }

        public Builder addPointLongitudeLatitude(double longitude, double latitude) {
            return addPoint(Point.fromLongitudeLatitude(longitude, latitude));
        }

        public Builder addPointLatitudeLongitude(double latitude, double longitude) {
            return addPoint(Point.fromLatitudeLongitude(latitude, longitude));
        }

        public Builder addPoints(Collection<Point> points) {
            this.points.addAll(points);
            return this;
        }

        public Builder addPoints(Point... points) {
            Collections.addAll(this.points, points);
            return this;
        }

        public Builder removePoint(Point point) {
            points.remove(point);
            return this;
        }

        public Builder removePoints(Collection<Point> points) {
            this.points.removeAll(points);
            return this;
        }

        public Builder removePoints(Point... points) {
            for (Point point : points) {
                removePoint(point);
            }
            return this;
        }

        public MultiPoint build() {
            return new MultiPoint(ImmutableList.copyOf(this.points));
        }
    }

}
