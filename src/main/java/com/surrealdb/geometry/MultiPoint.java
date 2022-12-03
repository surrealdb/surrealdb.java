package com.surrealdb.geometry;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

import static com.surrealdb.geometry.InternalGeometryUtils.calculateCenterOfPointsIterable;

/**
 * MultiPoints can be used to store multiple geometry points in a single value.
 *
 * @see <a href="https://surrealdb.com/docs/surrealql/datamodel/geometries#multipoint">Surreal Docs - MultiPoint</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7946#section-3.1.3">GeoJSON - MultiPoint</a>
 */
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class MultiPoint extends Geometry implements Iterable<Point> {

    public static final MultiPoint EMPTY = new MultiPoint(ImmutableList.of());

    @NotNull ImmutableList<Point> points;

    /**
     * @param points The points to store in this MultiPoint.
     * @return A new MultiPoint with the given points.
     * @throws NullPointerException If {@code points} contains a null value.
     */
    public static @NotNull MultiPoint from(@NotNull Collection<Point> points) {
        if (points.isEmpty()) {
            return EMPTY;
        }

        return new MultiPoint(ImmutableList.copyOf(points));
    }

    /**
     * @param points The points to store in this MultiPoint.
     * @return A new MultiPoint with the given points.
     * @throws NullPointerException If {@code points} contains a null value.
     */
    public static @NotNull MultiPoint from(Point @NotNull ... points) {
        if (points.length == 0) {
            return EMPTY;
        }

        return new MultiPoint(ImmutableList.copyOf(points));
    }

    /**
     * @return A new {@link MultiPoint.Builder} instance
     */
    public static @NotNull Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new {@link MultiPoint.Builder} with all points from this {@code MultiPoint}.
     *
     * @return A new {@link MultiPoint.Builder} instance with the points of this {@code MultiPoint}.
     */
    public @NotNull Builder toBuilder() {
        return builder().addPoints(points);
    }

    public int calculatePointCount() {
        return points.size();
    }

    public @NotNull Point getPoint(int index) {
        return points.get(index);
    }

    @Override
    public @NotNull Iterator<Point> iterator() {
        return points.iterator();
    }

    public @NotNull MultiPoint translate(double x, double y) {
        return transform(point -> point.add(x, y));
    }

    public @NotNull MultiPoint rotate(@NotNull Point origin, double radians) {
        return transform((point -> point.rotateDegrees(origin, radians)));
    }

    public @NotNull MultiPoint rotate(double radians) {
        return rotate(getCenter(), radians);
    }

    public @NotNull MultiPoint rotateDegrees(@NotNull Point origin, double degrees) {
        return transform((point -> point.rotateDegrees(origin, degrees)));
    }

    public @NotNull MultiPoint rotateDegrees(double degrees) {
        return rotateDegrees(getCenter(), degrees);
    }

    public @NotNull MultiPoint scale(@NotNull Point center, double factorX, double factorY) {
        return transform(point -> point.scale(center, factorX, factorY));
    }

    public @NotNull MultiPoint scale(@NotNull Point center, double factor) {
        return scale(center, factor, factor);
    }

    public @NotNull MultiPoint scale(double factorX, double factorY) {
        return scale(getCenter(), factorX, factorY);
    }

    public @NotNull MultiPoint scale(double factor) {
        return scale(getCenter(), factor, factor);
    }

    public @NotNull MultiPoint transform(@NotNull Function<Point, Point> transform) {
        if (this == EMPTY) {
            return EMPTY;
        }

        List<Point> transformedPoints = new ArrayList<>(points.size());
        for (Point point : points) {
            transformedPoints.add(transform.apply(point));
        }

        return MultiPoint.from(transformedPoints);
    }

    @Override
    protected @NotNull String calculateWkt() {
        return InternalGeometryUtils.calculateWktGeometryRepresentationPoints("MULTIPOINT", this);
    }

    protected @NotNull Point calculateCenter() {
        return calculateCenterOfPointsIterable(this);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static class Builder {

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
         * Convenience method for adding a point in the form {@code (x, y)}.
         *
         * @param x The x of the point
         * @param y The y of the point
         * @return This {@code Builder} object
         */
        public @NotNull Builder addPointXY(double x, double y) {
            return addPoint(Point.fromXY(x, y));
        }

        /**
         * Convenience method for adding a point in the form {@code (y, x)}.
         *
         * @param y The y of the point
         * @param x The x of the point
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
         * Creates and returns a new {@link MultiPoint} instance with the points added to this {@code Builder}. The
         * {@code Builder's} backing list is copied, meaning that changes to this {@code Builder} will not be reflected
         * the returned {@code MultiPoint}.
         *
         * @return A new {@link MultiPoint} with the points added to this {@code Builder}
         */
        public @NotNull MultiPoint build() {
            return MultiPoint.from(points);
        }
    }
}
