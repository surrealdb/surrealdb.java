package com.surrealdb.driver.model.geometry;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

/**
 * SurrealDB's representation of a geolocation point. This is a 2D point with a longitude and latitude.
 * <p>To create a point, use: <p>
 * <ul>
 *  <li>{@link #fromLatitudeLongitude(double, double)}</li>
 *  <li>{@link #fromLatitudeLongitude(double, double)}</li>
 * </ul>
 *
 * @author Damian Kocher
 * @see <a href="https://surrealdb.com/docs/surrealql/datamodel/geometries#point">SurrealDB Docs - Point</a>
 * @see <a href="https://www.rfc-editor.org/rfc/rfc7946#section-3.1.1">GeoJSON Specification - Point</a>
 * @see <a href="https://en.wikipedia.org/wiki/Geographic_coordinate_system">Geographical Coordinate System (Wikipedia)</a>
 */
@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@With
public class Point implements GeometryPrimitive {

    double longitude;
    double latitude;

    /**
     * This static factory method returns a {@link Point} from the provided <i>longitude</i> and <i>latitude</i> (in that order).
     * If you would like to provide <i>latitude</i> first, use {@link #fromLatitudeLongitude(double, double)} instead.
     *
     * @param latitude  The latitude of the point.
     * @param longitude The longitude of the point.
     * @return A new SurrealPoint.
     * @see #fromLatitudeLongitude(double, double)
     */
    public static Point fromLongitudeLatitude(double longitude, double latitude) {
        return new Point(longitude, latitude);
    }

    /**
     * This static factory method returns a {@link Point} from the provided <i>latitude</i> and <i>longitude</i> (in that order).
     * If you would like to provide <i>longitude</i> first, use {@link #fromLongitudeLatitude(double, double)} instead.
     *
     * @param latitude  The latitude of the point.
     * @param longitude The longitude of the point.
     * @return A new SurrealPoint.
     * @see #fromLongitudeLatitude(double, double)
     */
    public static Point fromLatitudeLongitude(double latitude, double longitude) {
        return new Point(longitude, latitude);
    }
}
