package com.surrealdb.driver.model.geometry;

import com.google.common.collect.ImmutableList;
import lombok.Value;

import javax.annotation.Nullable;
import java.util.Optional;

@Value
public class SurrealPolygon implements SurrealGeometryPrimitive {

    ImmutableList<SurrealPoint> outerRing;
    @Nullable
    ImmutableList<SurrealPoint> innerRing;

    public SurrealPolygon(ImmutableList<SurrealPoint> outerRing, @Nullable ImmutableList<SurrealPoint> innerRing) {
        validateLinearRing(outerRing, false, "Outer ring");
        this.outerRing = outerRing;

        if (innerRing != null) {
            validateLinearRing(innerRing, true, "Inner ring");
            this.innerRing = innerRing;
        } else {
            this.innerRing = null;
        }
    }

    public SurrealPolygon(ImmutableList<SurrealPoint> outerRing) {
        this(outerRing, null);
    }

    public static SurrealPolygon fromOuterRing(SurrealPoint... outerRing) {
        return new SurrealPolygon(ImmutableList.copyOf(outerRing), null);
    }

    public Optional<ImmutableList<SurrealPoint>> getInnerRing() {
        return Optional.ofNullable(innerRing);
    }

    private void validateLinearRing(ImmutableList<SurrealPoint> points, boolean clockwise, String ringType) {
        if (points.size() < 3) {
            throw new IllegalArgumentException(String.format("%s must have at least 3 points", ringType));
        }

        if (calculateSum(points) > 0 != clockwise) {
            throw new IllegalArgumentException(String.format("%s must be %s", ringType, clockwise ? "clockwise" : "counter-clockwise"));
        }
    }

    private double calculateSum(ImmutableList<SurrealPoint> ring) {
        double sum = 0;
        for (int i = 0; i < ring.size() - 1; i++) {
            SurrealPoint p1 = ring.get(i);
            SurrealPoint p2 = ring.get(i + 1);
            sum += (p2.getLongitude() - p1.getLongitude()) * (p2.getLatitude() + p1.getLatitude());
        }
        return sum;
    }
}
