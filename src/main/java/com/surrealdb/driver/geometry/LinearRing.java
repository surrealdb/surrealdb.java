package com.surrealdb.driver.geometry;

import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Iterator;

@EqualsAndHashCode(callSuper = true)
@ToString
public class LinearRing extends LineString {

    boolean closed;
    int pointCount;

    private LinearRing(@NotNull ImmutableList<Point> points) {
        super(points);

        closed = isClosedLinearRing();
        pointCount = points.size() + (closed ? 0 : 1);
    }

    public static @NonNull LinearRing from(@NotNull Collection<Point> points) {
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
    public @NonNull Iterator<Point> iterator() {
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

    private static class LinearRingIterator implements Iterator<Point> {

        LinearRing linearRing;
        @NonFinal
        int index = 0;

        private LinearRingIterator(LinearRing linearRing) {
            this.linearRing = linearRing;
        }

        @Override
        public boolean hasNext() {
            return index < linearRing.getPointCount();
        }

        @Override
        public @NonNull Point next() {
            return linearRing.getPoint(index++);
        }
    }
}
