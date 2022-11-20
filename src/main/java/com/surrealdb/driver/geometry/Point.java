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
