package com.surrealdb.driver.geometry;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

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
