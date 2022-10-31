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
 * A GeoJSON LineString value for storing a geometric path. Paths must have at least two points,
 * but may have more.
 *
 * @author Damian Kocher
 * @see <a href="https://surrealdb.com/docs/surrealql/datamodel/geometries#line">SurrealDB Docs - Line</a>
 * @see <a href="https://tools.ietf.org/html/rfc7946#section-3.1.4">GeoJSON Specification - LineString</a>
 */
@Value
public class Line implements GeometryPrimitive {

    ImmutableList<Point> points;

    private Line(ImmutableList<Point> points) {
        this.points = points;

        if (points.size() < 2) {
            throw new IllegalArgumentException("Line must have at least 2 points");
        }
    }

    /**
     * Creates and returns a new {@code Line} with the given points. Elements from the provided {@code Collection} are copied,
     * meaning that changes to the provided {@code Collection} will not be reflected in the returned {@code Line}.
     *
     * @param points The points that make up the line
     * @return A new Line instance
     * @throws IllegalArgumentException If the provided {@code points} contains less than 2 elements
     * @throws NullPointerException     If any of the points are null
     */
    public static Line from(Collection<Point> points) {
        return new Line(ImmutableList.copyOf(points));
    }

    /**
     * Creates and returns a new {@code Line} with the given points. Elements from the provided {@code array} are copied,
     * meaning that changes to the provided {@code array} will not be reflected in the returned {@code Line}.
     *
     * @param points The points that make up the line
     * @return A new Line instance
     * @throws IllegalArgumentException If the provided {@code points} contains less than 2 elements
     * @throws NullPointerException     If any of the points are null
     */
    public static Line from(Point... points) {
        return new Line(ImmutableList.copyOf(points));
    }

    /**
     * @return A new {@link Line.Builder} instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new {@link Line.Builder} with all points on this line already added.
     *
     * @return A new {@link Line.Builder} instance with the points of this line.
     */
    public Builder toBuilder() {
        return new Builder().addPoints(points);
    }

    /**
     * Flips the order of the points in this line. For a line with the points
     * {@code [[0, 0,], [1, 1], [2, 2]]}, this will return a line with
     * {@code [[2, 2], [1, 1], [0, 0]]}.
     *
     * @return A new {@link Line} with the points in reverse order.
     */
    public Line flip() {
        return new Line(points.reverse());
    }

    /**
     * A builder for fluent construction of {@link Line} instances. Use {@link Line#builder()} to create a new empty
     * instance, or {@link Line#toBuilder()} to create a new instance with the points of an existing {@link Line}.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {

        private final List<Point> points = new ArrayList<>();

        /**
         * @param point The point to add
         * @return This {@code Builder} object
         */
        public Builder addPoint(Point point) {
            points.add(point);
            return this;
        }

        /**
         * Convenience method for adding a point in the form {@code x, y}.
         *
         * @param x The x of the point to add
         * @param y The y of the point to add
         * @return This {@code Builder} object
         */
        public Builder addPointXY(double x, double y) {
            return addPoint(Point.fromXY(x, y));
        }

        /**
         * Convenience method for adding a point in the form {@code y, x}.
         *
         * @param y The y of the point to add
         * @param x The x of the point to add
         * @return This {@code Builder} object
         */
        public Builder addPointYX(double y, double x) {
            return addPoint(Point.fromYX(y, x));
        }

        /**
         * @param points The points to add
         * @return This {@code Builder} object
         */
        public Builder addPoints(Collection<Point> points) {
            this.points.addAll(points);
            return this;
        }

        /**
         * @param points The points to add
         * @return This {@code Builder} object
         */
        public Builder addPoints(Point... points) {
            Collections.addAll(this.points, points);
            return this;
        }

        /**
         * @param point The point to remove
         * @return This {@code Builder} object
         */
        public Builder removePoint(Point point) {
            points.remove(point);
            return this;
        }

        /**
         * @param points The points to remove
         * @return This {@code Builder} object
         */
        public Builder removePoints(Collection<Point> points) {
            this.points.removeAll(points);
            return this;
        }

        /**
         * @param points The points to remove
         * @return This {@code Builder} object
         */
        public Builder removePoints(Point... points) {
            for (Point point : points) {
                this.points.remove(point);
            }
            return this;
        }

        /**
         * Creates and returns a new {@code Line} with the geometries added to this {@code Builder}.
         * This Builder's backing list is copied, meaning that this Builder can be reused after calling this method.
         *
         * @return A new {@link Line} instance with the points added to this builder.
         * @throws IllegalArgumentException If the line has less than 2 points.
         */
        public Line build() {
            return Line.from(points);
        }
    }
}
