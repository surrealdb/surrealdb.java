package com.surrealdb.driver.model.geometry;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Value;

import javax.annotation.Nullable;
import java.util.ArrayList;
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

    ImmutableList<Point> outerRing;
    ImmutableList<Point> innerRing;

    private Polygon(ImmutableList<Point> outerRing, @Nullable ImmutableList<Point> innerRing) {
        validateLinearRing(outerRing, "Outer ring");
        this.outerRing = outerRing;

        if (innerRing != null) {
            validateLinearRing(innerRing, "Inner ring");
            this.innerRing = innerRing;
        } else {
            this.innerRing = ImmutableList.of();
        }
    }

    public static Polygon fromOuterAndInnerRing(ImmutableList<Point> outerRing, @Nullable ImmutableList<Point> innerRing) {
        return new Polygon(outerRing, innerRing);
    }

    public static Polygon fromOuterRing(ImmutableList<Point> outerRing) {
        return new Polygon(outerRing, null);
    }

    public static Builder builder() {
        return new Builder();
    }

    private void validateLinearRing(ImmutableList<Point> points, String ringType) {
        if (points.size() < 4) {
            throw new IllegalArgumentException(String.format("%s must have at least 4 points", ringType));
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Builder {

        private final List<Point> outerRing = new ArrayList<>();
        private final List<Point> innerRing = new ArrayList<>();

        public Builder addOuterRingPoint(Point point) {
            outerRing.add(point);
            return this;
        }

        public Builder addOuterRingPointLongitudeLatitude(double longitude, double latitude) {
            Point point = Point.fromLongitudeLatitude(longitude, latitude);
            return addOuterRingPoint(point);
        }

        public Builder addOuterRingPointLatitudeLongitude(double latitude, double longitude) {
            Point point = Point.fromLatitudeLongitude(longitude, latitude);
            return addInnerRingPoint(point);
        }

        public Builder addInnerRingPoint(Point point) {
            innerRing.add(point);
            return this;
        }

        public Builder addInnerRingPointLongitudeLatitude(double longitude, double latitude) {
            Point point = Point.fromLongitudeLatitude(longitude, latitude);
            return addInnerRingPoint(point);
        }

        public Builder addInnerRingPointLatitudeLongitude(double latitude, double longitude) {
            Point point = Point.fromLatitudeLongitude(latitude, longitude);
            return addInnerRingPoint(point);
        }

        public Polygon build() {
            return new Polygon(ImmutableList.copyOf(outerRing), ImmutableList.copyOf(innerRing));
        }
    }
}
