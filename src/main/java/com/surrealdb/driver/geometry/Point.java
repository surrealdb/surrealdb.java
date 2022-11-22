package com.surrealdb.driver.geometry;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.NotNull;

import static com.surrealdb.driver.geometry.InternalGeometryUtils.calculateWktPoint;

/**
 * SurrealDB's representation of a geolocation point. This is a 2D point with a longitude and latitude.
 * <p>To create a point, use: <p>
 * <ul>
 *  <li>{@link #fromYX(double, double)}</li>
 *  <li>{@link #fromYX(double, double)}</li>
 * </ul>
 *
 * @see <a href="https://surrealdb.com/docs/surrealql/datamodel/geometries#point">SurrealDB Docs - Point</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7946#section-3.1.1">GeoJSON Specification - Point</a>
 * @see <a href="https://en.wikipedia.org/wiki/Geographic_coordinate_system">Geographical Coordinate System (Wikipedia)</a>
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false)
public final class Point extends GeometryPrimitive {

    public static final @NotNull Point ZERO = new Point(0, 0);

    // https://nssdc.gsfc.nasa.gov/planetary/factsheet/earthfact.html
    // Volumetric mean radius (km) = 6371
    private static final double EARTH_RADIUS = 6371;

    double x;
    double y;

    /**
     * This static factory method returns a {@link Point} from the provided <i>x</i> and <i>y</i> (in that order).
     * If you would like to provide <i>y</i> first, use {@link #fromYX(double, double)} instead.
     *
     * @param y The y of the point
     * @param x The x of the point
     * @return A new SurrealPoint
     * @see #fromYX(double, double)
     */
    public static @NotNull Point fromXY(double x, double y) {
        return new Point(x, y);
    }

    /**
     * This static factory method returns a {@link Point} from the provided <i>y</i> and <i>x</i> (in that order).
     * If you would like to provide <i>x</i> first, use {@link #fromXY(double, double)} instead.
     *
     * @param y The y of the point
     * @param x The x of the point
     * @return A new SurrealPoint
     * @see #fromXY(double, double)
     */
    public static @NotNull Point fromYX(double y, double x) {
        return new Point(x, y);
    }

    public static @NotNull Point fromGeoHash(@NonNull String geoHash) {
        DecodingGeoHashCoord xCoord = new DecodingGeoHashCoord(-180, 180);
        DecodingGeoHashCoord yCoord = new DecodingGeoHashCoord(-90, 90);
        boolean lng = true;

        for (char character : Lists.charactersOf(geoHash)) {
            int value = decode(character);

            for (int shift = 4; shift >= 0; --shift) {
                boolean isMin = ((value >> shift) & 1) == 1;

                DecodingGeoHashCoord coord = lng ? xCoord : yCoord;
                coord.cut(isMin);

                lng = !lng;
            }
        }

        return new Point(xCoord.mid(), yCoord.mid());
    }

    private static int decode(char character) {
        if (character >= '0' && character <= '9') {
            return character - '0';
        }
        if (character >= 'b' && character <= 'h') {
            return character - 'b' + 10;
        }
        if (character >= 'j' && character <= 'l') {
            return character - 'j' + 17;
        }
        if (character >= 'm' && character <= 'n') {
            return character - 'm' + 19;
        }
        if (character >= 'p' && character <= 'z') {
            return character - 'p' + 21;
        }
        throw new IllegalArgumentException("Invalid character in geo hash: " + character);
    }

    public static double distanceInKilometers(Point p1, Point p2) {
        // https://en.wikipedia.org/wiki/Haversine_formula
        // Return distance in meters

        double lat1 = p1.y;
        double lon1 = p1.x;
        double lat2 = p2.y;
        double lon2 = p2.x;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.pow(Math.sin(dLon / 2), 2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return EARTH_RADIUS * c;
    }

    public static double distanceInMeters(Point p1, Point p2) {
        return distanceInKilometers(p1, p2) * 1000;
    }

    /**
     * @param newX The newX of the new point
     * @return A new point with the same latitude as this point, but with the provided newX.
     */
    public @NotNull Point withX(double newX) {
        return new Point(newX, y);
    }

    /**
     * @param newY The newY of the new point
     * @return A new point with the same longitude as this point, but with the provided newY.
     */
    public @NotNull Point withY(double newY) {
        return new Point(x, newY);
    }

    public @NotNull String toGeoHash(int precision) {
        EncodedGeoHashCoord xCoord = new EncodedGeoHashCoord(-180, 180, x);
        EncodedGeoHashCoord yCoord = new EncodedGeoHashCoord(-90, 90, y);
        StringBuilder geoHash = new StringBuilder(precision);
        boolean lng = true;

        while (geoHash.length() < precision) {
            int hash = 0;

            for (int shift = 4; shift >= 0; --shift) {
                EncodedGeoHashCoord coord = lng ? xCoord : yCoord;
                hash = (hash << 1) + (coord.cut() ? 1 : 0);
                lng = !lng;
            }

            geoHash.append(encodeGeoHashChar(hash));
        }

        return geoHash.toString();
    }

    private char encodeGeoHashChar(int value) {
        if (value >= 0 && value <= 9) {
            return (char) (value + '0');
        }
        if (value >= 10 && value <= 16) {
            return (char) (value + 'b' - 10);
        }
        if (value >= 17 && value <= 18) {
            return (char) (value + 'j' - 17);
        }
        if (value >= 19 && value <= 20) {
            return (char) (value + 'm' - 19);
        }
        if (value >= 21 && value <= 31) {
            return (char) (value + 'p' - 21);
        }

        throw new IllegalArgumentException("Invalid value in geo hash: " + value);
    }

    public double distanceInKilometers(@NotNull Point other) {
        return distanceInKilometers(this, other);
    }

    public double distanceInMeters(@NotNull Point other) {
        return distanceInMeters(this, other);
    }

    public @NotNull Point add(@NotNull Point other) {
        return add(other.x, other.y);
    }

    public @NotNull Point add(double x, double y) {
        return new Point(this.x + x, this.y + y);
    }

    public @NotNull Point subtract(@NotNull Point other) {
        return subtract(other.x, other.y);
    }

    public @NotNull Point subtract(double x, double y) {
        return new Point(this.x - x, this.y - y);
    }

    public @NotNull Point rotate(Point center, double angle) {
        double x = center.getX() + (this.getX() - center.getX()) * Math.cos(angle) - (this.getY() - center.getY()) * Math.sin(angle);
        double y = center.getY() + (this.getX() - center.getX()) * Math.sin(angle) + (this.getY() - center.getY()) * Math.cos(angle);

        return Point.fromXY(x, y);
    }

    public @NotNull Point rotate(double angle) {
        return rotate(ZERO, angle);
    }

    public @NotNull Point scale(Point center, double scaleX, double scaleY) {
        double x = center.getX() + (this.getX() - center.getX()) * scaleX;
        double y = center.getY() + (this.getY() - center.getY()) * scaleY;

        return Point.fromXY(x, y);
    }

    public @NotNull Point scale(Point center, double scale) {
        return scale(center, scale, scale);
    }

    public @NotNull Point scale(double scaleX, double scaleY) {
        return scale(ZERO, scaleX, scaleY);
    }

    public @NotNull Point scale(double scale) {
        return scale(ZERO, scale, scale);
    }

    /**
     * @return The longitude of the point
     */
    public double getX() {
        return x;
    }

    /**
     * @return The latitude of the point
     */
    public double getY() {
        return y;
    }

    @Override
    protected @NotNull String calculateWkt() {
        return calculateWktPoint("POINT", this);
    }

    @AllArgsConstructor
    private abstract static class GeoHashCoord {

        @NonFinal
        double min;
        @NonFinal
        double max;

        public double mid() {
            return (min + max) / 2;
        }
    }


    private static final class EncodedGeoHashCoord extends GeoHashCoord {

        double actual;

        public EncodedGeoHashCoord(double min, double max, double actual) {
            super(min, max);
            this.actual = actual;
        }

        boolean cut() {
            double mid = mid();

            if (actual > mid) {
                super.min = mid;
                return true;
            } else {
                super.max = mid;
                return false;
            }
        }
    }

    private static final class DecodingGeoHashCoord extends GeoHashCoord {

        public DecodingGeoHashCoord(double min, double max) {
            super(min, max);
        }

        void cut(boolean isMin) {
            if (isMin) {
                super.min = mid();
            } else {
                super.max = mid();
            }
        }
    }
}
