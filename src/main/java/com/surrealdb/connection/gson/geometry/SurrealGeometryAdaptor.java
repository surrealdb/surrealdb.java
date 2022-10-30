package com.surrealdb.connection.gson.geometry;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.surrealdb.connection.gson.SurrealGsonAdaptor;
import com.surrealdb.driver.model.geometry.SurrealGeometryPrimitive;
import com.surrealdb.driver.model.geometry.SurrealLineString;
import com.surrealdb.driver.model.geometry.SurrealPoint;
import com.surrealdb.driver.model.geometry.SurrealPolygon;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class SurrealGeometryAdaptor<T extends SurrealGeometryPrimitive> extends SurrealGsonAdaptor<T> {

    protected SurrealGeometryAdaptor(Class<T> adaptorClass) {
        super(adaptorClass);
    }

    protected JsonObject createJsonObject(String type, JsonArray coordinates) {
        JsonObject object = new JsonObject();
        object.addProperty("type", type);
        object.add("coordinates", coordinates);
        return object;
    }

    protected JsonArray getCoordinates(JsonElement element) {
        JsonObject object = element.getAsJsonObject();
        return object.getAsJsonArray("coordinates");
    }

    protected JsonArray serializePointToArray(SurrealPoint point) {
        return serializePointToArray(point.getLongitude(), point.getLatitude());
    }

    protected JsonArray serializePointToArray(double longitude, double latitude) {
        JsonArray pointArray = new JsonArray();
        pointArray.add(longitude);
        pointArray.add(latitude);
        return pointArray;
    }

    protected SurrealPoint deserializePointFromArray(JsonArray coordinates) {
        double longitude = coordinates.get(0).getAsDouble();
        double latitude = coordinates.get(1).getAsDouble();
        return SurrealPoint.fromLongitudeLatitude(longitude, latitude);
    }

    protected JsonArray serializeLineStringToArray(SurrealLineString lineString) {
        JsonArray lineStringArray = new JsonArray();
        for (SurrealPoint point : lineString.getPoints()) {
            lineStringArray.add(serializePointToArray(point));
        }
        return lineStringArray;
    }

    protected SurrealLineString deserializeLineStringFromArray(JsonArray coordinates) {
        List<SurrealPoint> points = new ArrayList<>(coordinates.size());
        for (JsonElement pointCoordinatesElement : coordinates) {
            points.add(deserializePointFromArray(pointCoordinatesElement.getAsJsonArray()));
        }
        return new SurrealLineString(ImmutableList.copyOf(points));
    }

    protected JsonArray serializePolygonToArray(SurrealPolygon polygon) {
        JsonArray coordinates = new JsonArray();
        coordinates.add(serializeLinearRing(polygon.getOuterRing()));

        Optional<ImmutableList<SurrealPoint>> innerRing = polygon.getInnerRing();
        innerRing.ifPresent(surrealPoints -> coordinates.add(serializeLinearRing(surrealPoints)));
        return coordinates;
    }

    private JsonArray serializeLinearRing(ImmutableList<SurrealPoint> linearRing) {
        JsonArray array = new JsonArray();
        for (SurrealPoint point : linearRing) {
            array.add(serializePointToArray(point));
        }
        // Add the first point again to close the ring
        array.add(array.get(0));

        return array;
    }

    protected SurrealPolygon deserializePolygonFromArray(JsonArray coordinates) {
        ImmutableList<SurrealPoint> outerRing = deserializeLinearRing(coordinates.get(0).getAsJsonArray());
        ImmutableList<SurrealPoint> innerRing = null;
        if (coordinates.size() > 1) {
            innerRing = deserializeLinearRing(coordinates.get(1).getAsJsonArray());
        }
        return new SurrealPolygon(outerRing, innerRing);
    }

    private ImmutableList<SurrealPoint> deserializeLinearRing(JsonElement element) {
        JsonArray pointCoordinates = element.getAsJsonArray();
        List<SurrealPoint> points = new ArrayList<>(pointCoordinates.size());

        // Ignore the last point, as it is the same as the first
        for (int index = 0; index < pointCoordinates.size() - 1; index++) {
            points.add(deserializePointFromArray(pointCoordinates.get(index).getAsJsonArray()));
        }

        return ImmutableList.copyOf(points);
    }
}
