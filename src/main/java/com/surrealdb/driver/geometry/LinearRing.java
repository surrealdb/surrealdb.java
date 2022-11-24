package com.surrealdb.driver.geometry;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

import static com.surrealdb.driver.geometry.InternalGeometryUtils.calculateWktGeometryRepresentationPoints;

public final class LinearRing extends GeometryPrimitive implements Iterable<Point> {

    @NotNull ImmutableList<Point> points;

    boolean closed;
    int pointCount;

    @Getter(lazy = true)
    private double circumferenceInKilometers = calculateCircumferenceInKilometers();

    private LinearRing(@NotNull ImmutableList<Point> points) {
        this.points = points;

        Point firstPoint = points.get(0);
        Point lastPoint = points.get(points.size() - 1);

        closed = firstPoint.equals(lastPoint);
        pointCount = points.size() + (closed ? 0 : 1);
    }

    public static @NotNull LinearRing from(@NotNull Collection<Point> points) {
        return new LinearRing(ImmutableList.copyOf(points));
    }

    public static @NotNull LinearRing from(Point @NotNull ... points) {
        return new LinearRing(ImmutableList.copyOf(points));
    }

    public static @NotNull LinearRing.Builder builder() {
        return new LinearRing.Builder();
    }

    public @NotNull LinearRing.Builder toBuilder() {
        return new LinearRing.Builder().addPoints(this.points);
    }

    @Override
    public @NotNull Iterator<Point> iterator() {
        return new LinearRingIterator(this);
    }

    public @NotNull LinearRing translate(double x, double y) {
        return transform((point) -> point.add(x, y));
    }

    public @NotNull LinearRing rotate(double degrees) {
        return rotate(getCenter(), degrees);
    }

    public @NotNull LinearRing rotate(@NotNull Point center, double degrees) {
        return transform(point -> point.rotateDegrees(center, degrees));
    }

    public @NotNull LinearRing scale(@NotNull Point center, double scaleX, double scaleY) {
        return transform(point -> point.scale(center, scaleX, scaleY));
    }

    public @NotNull LinearRing scale(double scaleX, double scaleY) {
        return scale(getCenter(), scaleX, scaleY);
    }

    public @NotNull LinearRing scale(@NotNull Point center, double scale) {
        return scale(center, scale, scale);
    }

    public @NotNull LinearRing scale(double scale) {
        return scale(scale, scale);
    }

    public @NotNull LinearRing transform(@NotNull Function<Point, Point> transformFunction) {
        List<Point> transformedPoints = new ArrayList<>(points.size());

        for (Point point : this) {
            transformedPoints.add(transformFunction.apply(point));
        }

        return LinearRing.from(transformedPoints);
    }

    @Override
    public int calculatePointCount() {
        return pointCount;
    }

    @Override
    protected @NotNull String calculateWkt() {
        return calculateWktGeometryRepresentationPoints("LINEARRING", this);
    }

    public @NotNull Point getPoint(int index) {
        if (!closed && index == pointCount - 1) {
            return points.get(0);
        }

        return points.get(index);
    }

    public @NotNull LineString toLineString () {
        return LineString.from(points);
    }

    private double calculateCircumferenceInKilometers() {
        double circumference = 0;

        for (int i = 0; i < pointCount - 1; i++) {
            Point point1 = getPoint(i);
            Point point2 = getPoint(i + 1);

            circumference += Point.distanceInKilometers(point1, point2);
        }

        return circumference;
    }

    public double getCircumferenceInMeters() {
        return getCircumferenceInKilometers() * 1000;
    }

    @Override
    protected @NotNull Point calculateCenter() {
        // Is there a cleaner way to do this? This is almost exactly the same
        // as the methods in InternalGeometryUtils. The last point MUST NOT
        // be included in the calculation, as it is the same as the first point.
        double x = 0;
        double y = 0;

        for (int i = 0; i < pointCount - 1; i++) {
            Point point = getPoint(i);

            x += point.getX();
            y += point.getY();
        }

        return Point.fromXY(x / pointCount, y / pointCount);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;

        if (other instanceof LinearRing otherLinearRing) {
            if (otherLinearRing.getPointCount() != pointCount) {
                return false;
            }

            for (int i = 0; i < pointCount; i++) {
                if (!getPoint(i).equals(otherLinearRing.getPoint(i))) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int result = 23;

        for (Point point : this) {
            result = 31 * result + point.hashCode();
        }

        return result;
    }

    @RequiredArgsConstructor
    private static class LinearRingIterator implements Iterator<Point> {

        @NotNull LinearRing linearRing;

        @NonFinal
        int index = 0;

        @Override
        public boolean hasNext() {
            return index < linearRing.getPointCount();
        }

        @Override
        public @NotNull Point next() {
            return linearRing.getPoint(index++);
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {

        @NotNull List<Point> points = new ArrayList<>();

        /**
         * @param point The point to add
         * @return This {@code Builder} object
         */
        public @NotNull LinearRing.Builder addPoint(Point point) {
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
        public @NotNull LinearRing.Builder addPointXY(double x, double y) {
            return addPoint(Point.fromXY(x, y));
        }

        /**
         * Convenience method for adding a point in the form {@code y, x}.
         *
         * @param y The y of the point to add
         * @param x The x of the point to add
         * @return This {@code Builder} object
         */
        public @NotNull LinearRing.Builder addPointYX(double y, double x) {
            return addPoint(Point.fromYX(y, x));
        }

        /**
         * @param points The points to add
         * @return This {@code Builder} object
         */
        public @NotNull LinearRing.Builder addPoints(@NotNull Collection<Point> points) {
            this.points.addAll(points);
            return this;
        }

        /**
         * @param points The points to add
         * @return This {@code Builder} object
         */
        public @NotNull LinearRing.Builder addPoints(Point... points) {
            Collections.addAll(this.points, points);
            return this;
        }

        /**
         * @param point The point to remove
         * @return This {@code Builder} object
         */
        public @NotNull LinearRing.Builder removePoint(Point point) {
            points.remove(point);
            return this;
        }

        /**
         * @param points The points to remove
         * @return This {@code Builder} object
         */
        public @NotNull LinearRing.Builder removePoints(@NotNull Collection<Point> points) {
            this.points.removeAll(points);
            return this;
        }

        /**
         * @param points The points to remove
         * @return This {@code Builder} object
         */
        public @NotNull LinearRing.Builder removePoints(Point @NotNull ... points) {
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
        public @NotNull LinearRing build() {
            return LinearRing.from(points);
        }
    }
}
