package com.surrealdb.driver.model.geometry;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.With;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@With
public class SurrealPoint implements SurrealGeometryPrimitive {

    double longitude;
    double latitude;

    /**
     * GeoJSON requires longitude to be first, then latitude. This is the opposite of the order
     * that the constructor takes, so this method is provided to make it easier to create
     * GeoJSON objects.
     *
     * @param latitude  The latitude of the point.
     * @param longitude The longitude of the point.
     * @return A new SurrealPoint.
     */
    public static SurrealPoint fromLatitudeLongitude(double latitude, double longitude) {
        return new SurrealPoint(longitude, latitude);
    }

    public static SurrealPoint fromLongitudeLatitude(double longitude, double latitude) {
        return new SurrealPoint(longitude, latitude);
    }
}
