package com.surrealdb.driver.geometry;

import com.google.common.collect.Lists;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * SurrealDB's representation of a geolocation point. This is a 2D point with a longitude and latitude.
 * <p>To create a point, use: <p>
 * <ul>
 *  <li>{@link #fromYX(double, double)}</li>
 *  <li>{@link #fromYX(double, double)}</li>
 * </ul>
 *
 * @author Damian Kocher
 * @see <a href="https://surrealdb.com/docs/surrealql/datamodel/geometries#point">SurrealDB Docs - Point</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7946#section-3.1.1">GeoJSON Specification - Point</a>
 * @see <a href="https://en.wikipedia.org/wiki/Geographic_coordinate_system">Geographical Coordinate System (Wikipedia)</a>
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
public class Point implements GeometryPrimitive {

    static Pattern GEO_HASH_PATTERN = Pattern.compile("[0-9bcdefghjkmnpqrstuvwxyz]+");

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

    public static @NotNull Point fromGeoHash(String geoHash) {
        if (!GEO_HASH_PATTERN.matcher(geoHash).matches()) {
            throw new IllegalArgumentException("Provided geo hash contains invalid characters: " + geoHash);
        }

        double xMin = -180;
        double xMax = 180;
        double yMin = -90;
        double yMax = 90;

        boolean lng = true;

        for (char character : Lists.charactersOf(geoHash)) {
            int value = decode(character);

            for (int shift = 4; shift >= 0; --shift) {
                boolean isMin = ((value >> shift) & 1) == 1;

                if (lng) {
                    double mid = average(xMin, xMax);
                    if (isMin) {
                        xMin = mid;
                    } else {
                        xMax = mid;
                    }
                } else {
                    double mid = average(yMin, yMax);
                    if (isMin) {
                        yMin = mid;
                    } else {
                        yMax = mid;
                    }
                }

                lng = !lng;
            }
        }

        return new Point(average(xMin, xMax), average(yMin, yMax));
    }

    private static double average(double a, double b) {
        return (a + b) / 2;
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
        double xMin = -180;
        double xMax = 180;
        double yMin = -90;
        double yMax = 90;

        int hash = 0;
        StringBuilder geoHash = new StringBuilder(precision);

        boolean lng = true;

        while (geoHash.length() < precision) {
            for (int shift = 4; shift >= 0; --shift) {
                if (lng) {
                    double mid = average(xMin, xMax);
                    if (x > mid) {
                        hash = (hash << 1) + 1;
                        xMin = mid;
                    } else {
                        hash <<= 1;
                        xMax = mid;
                    }
                } else {
                    double mid = average(yMin, yMax);
                    if (y > mid) {
                        hash = (hash << 1) + 1;
                        yMin = mid;
                    } else {
                        hash <<= 1;
                        yMax = mid;
                    }
                }

                lng = !lng;
            }

            geoHash.append(encode(hash));
            hash = 0;
        }

        return geoHash.toString();
    }

    private char encode(int value) {
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
}
