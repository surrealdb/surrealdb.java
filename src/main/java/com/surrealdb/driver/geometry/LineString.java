package com.surrealdb.driver.geometry;

import com.google.common.collect.ImmutableList;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * A GeoJSON LineString value for storing a geometric path. Paths must have at least two points,
 * but may have more.
 *
 * @author Damian Kocher
 * @see <a href="https://surrealdb.com/docs/surrealql/datamodel/geometries#line">SurrealDB Docs - Line</a>
 * @see <a href="https://tools.ietf.org/html/rfc7946#section-3.1.4">GeoJSON Specification - LineString</a>
 */
@ToString
@EqualsAndHashCode
public class LineString implements GeometryPrimitive, Iterable<Point> {

    protected @NotNull ImmutableList<Point> points;

    protected LineString(@NotNull ImmutableList<Point> points) {
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
    public static @NotNull LineString from(@NotNull Collection<Point> points) {
        return new LineString(ImmutableList.copyOf(points));
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
    public static @NotNull LineString from(Point @NotNull ... points) {
        return new LineString(ImmutableList.copyOf(points));
    }

    /**
     * @return A new {@link LineString.Builder} instance.
     */
    public static @NotNull LineString.Builder builder() {
        return new LineString.Builder();
    }

    /**
     * Creates a new {@link LineString.Builder} with all points on this line already added.
     *
     * @return A new {@link LineString.Builder} instance with the points of this line.
     */
    public @NotNull Builder toBuilder() {
        return new Builder().addPoints(points);
    }

    public @NotNull LinearRing toLinearRing() {
        return LinearRing.from(points);
    }

    public int getPointCount() {
        return points.size();
    }

    public @NotNull Point getPoint(int index) {
        return points.get(index);
    }

    /**
     * Flips the order of the points in this line. For a line with the points
     * {@code [[0, 0,], [1, 1], [2, 2]]}, this will return a line with
     * {@code [[2, 2], [1, 1], [0, 0]]}.
     *
     * @return A new {@link LineString} with the points in reverse order.
     */
    public @NotNull LineString flip() {
        return new LineString(points.reverse());
    }

    @Override
    public @NonNull Iterator<Point> iterator() {
        return points.iterator();
    }

    /**
     * A builder for fluent construction of {@link LineString} instances. Use {@link LineString#builder()} to create a new empty
     * instance, or {@link LineString#toBuilder()} to create a new instance with the points of an existing {@link LineString}.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {

        @NotNull List<Point> points = new ArrayList<>();

        /**
         * @param point The point to add
         * @return This {@code Builder} object
         */
        public @NotNull Builder addPoint(Point point) {
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
        public @NotNull Builder addPointXY(double x, double y) {
            return addPoint(Point.fromXY(x, y));
        }

        /**
         * Convenience method for adding a point in the form {@code y, x}.
         *
         * @param y The y of the point to add
         * @param x The x of the point to add
         * @return This {@code Builder} object
         */
        public @NotNull Builder addPointYX(double y, double x) {
            return addPoint(Point.fromYX(y, x));
        }

        /**
         * @param points The points to add
         * @return This {@code Builder} object
         */
        public @NotNull Builder addPoints(@NotNull Collection<Point> points) {
            this.points.addAll(points);
            return this;
        }

        /**
         * @param points The points to add
         * @return This {@code Builder} object
         */
        public @NotNull Builder addPoints(Point... points) {
            Collections.addAll(this.points, points);
            return this;
        }

        /**
         * @param point The point to remove
         * @return This {@code Builder} object
         */
        public @NotNull Builder removePoint(Point point) {
            points.remove(point);
            return this;
        }

        /**
         * @param points The points to remove
         * @return This {@code Builder} object
         */
        public @NotNull Builder removePoints(@NotNull Collection<Point> points) {
            this.points.removeAll(points);
            return this;
        }

        /**
         * @param points The points to remove
         * @return This {@code Builder} object
         */
        public @NotNull Builder removePoints(Point @NotNull ... points) {
            for (Point point : points) {
                this.points.remove(point);
            }
            return this;
        }

        /**
         * Creates and returns a new {@code Line} with the geometries added to this {@code Builder}.
         * This Builder's backing list is copied, meaning that this Builder can be reused after calling this method.
         *
         * @return A new {@link LineString} instance with the points added to this builder.
         * @throws IllegalArgumentException If the line has less than 2 points.
         */
        public @NotNull LineString build() {
            return LineString.from(points);
        }

        public @NotNull LinearRing buildLinearRing() {
            return LinearRing.from(points);
        }
    }
}
