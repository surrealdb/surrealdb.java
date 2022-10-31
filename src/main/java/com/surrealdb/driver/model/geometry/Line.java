package com.surrealdb.driver.model.geometry;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.With;

import java.util.ArrayList;
import java.util.List;

/**
 * A GeoJSON LineString value for storing a geometric path.
 *
 * @author Damian Kocher
 * @see <a href="https://surrealdb.com/docs/surrealql/datamodel/geometries#line">SurrealDB Docs - Line</a>
 * @see <a href="https://tools.ietf.org/html/rfc7946#section-3.1.4">GeoJSON Specification - LineString</a>
 */
@Value
@With
public class Line implements GeometryPrimitive {

    ImmutableList<Point> points;

    private Line(ImmutableList<Point> points) {
        this.points = points;

        if (points.size() < 2) {
            throw new IllegalArgumentException("Line must have at least 2 points");
        }
    }

    /**
     * @param points The points that make up the line.
     * @return A new Line instance.
     * @throws IllegalArgumentException If the line has less than 2 points.
     * @throws NullPointerException     If any of the points are null.
     */
    public static Line fromPoints(List<Point> points) {
        return new Line(ImmutableList.copyOf(points));
    }

    /**
     * @param points The points that make up the line.
     * @return A new Line instance.
     * @throws IllegalArgumentException If the line has less than 2 points.
     * @throws NullPointerException     If any of the points are null.
     */
    public static Line fromPoints(Point... points) {
        return new Line(ImmutableList.copyOf(points));
    }

    /**
     * @return A new {@link Line.Builder} instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new {@link Line.Builder} with all points on this line string. This is the preferred way
     * to mutate a line string.
     *
     * @return A new {@link Line.Builder} instance with the points of this line.
     */
    public Builder toBuilder() {
        return new Builder().addPoints(points);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static class Builder {

        private final List<Point> points = new ArrayList<>();

        /**
         * @param point The point to add to the line.
         * @return This builder.
         */
        public Builder addPoint(Point point) {
            points.add(point);
            return this;
        }

        public Builder addPoints(List<Point> points) {
            this.points.addAll(points);
            return this;
        }

        public Builder removePoint(Point point) {
            points.remove(point);
            return this;
        }

        public Builder removePoint(int index) {
            points.remove(index);
            return this;
        }

        public Line build() {
            return Line.fromPoints(points);
        }
    }
}
