package com.surrealdb.driver.geometry;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;

public final class LinearRing extends LineString {

    boolean closed;
    int pointCount;

    @Getter(lazy = true)
    private double circumferenceInKilometers = calculateCircumferenceInKilometers();

    private LinearRing(@NotNull ImmutableList<Point> points) {
        super(points);

        closed = isClosedLinearRing();
        pointCount = points.size() + (closed ? 0 : 1);
    }

    public static @NotNull LinearRing from(@NotNull Collection<Point> points) {
        return new LinearRing(ImmutableList.copyOf(points));
    }

    public static @NotNull LinearRing from(Point @NotNull ... points) {
        return new LinearRing(ImmutableList.copyOf(points));
    }

    public boolean isClosedLinearRing() {
        // Line Strings are guaranteed to have at least 2 points
        Point firstPoint = getPoint(0);
        Point lastPoint = getPoint(super.getPointCount() - 1);

        return firstPoint.equals(lastPoint);
    }

    @Override
    public @NotNull Iterator<Point> iterator() {
        return new LinearRingIterator(this);
    }

    @Override
    public int getPointCount() {
        return pointCount;
    }

    @Override
    public @NotNull Point getPoint(int index) {
        if (!closed && index == super.getPointCount()) {
            return super.getPoint(0);
        }

        return super.getPoint(index);
    }

    @Override
    public @NotNull LinearRing toLinearRing() {
        return this;
    }

    private double calculateCircumferenceInKilometers() {
        double circumference = 0;

        for (int i = 0; i < getPointCount() - 1; i++) {
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
    public boolean equals(Object other) {
        if (this == other) return true;

        if (other instanceof LineString otherLineString) {
            if (otherLineString.getPointCount() != getPointCount()) {
                return false;
            }

            for (int i = 0; i < getPointCount(); i++) {
                if (!getPoint(i).equals(otherLineString.getPoint(i))) {
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
        int result = 0;

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
}
