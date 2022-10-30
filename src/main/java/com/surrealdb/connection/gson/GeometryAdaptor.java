package com.surrealdb.connection.gson;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.surrealdb.driver.model.geometry.SurrealGeometryPrimitive;
import com.surrealdb.driver.model.geometry.SurrealLineString;
import com.surrealdb.driver.model.geometry.SurrealPoint;
import com.surrealdb.driver.model.geometry.SurrealPolygon;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

abstract class GeometryAdaptor<T extends SurrealGeometryPrimitive> extends SurrealGsonAdaptor<T> {

    GeometryAdaptor(Class<T> adaptorClass) {
        super(adaptorClass);
    }

    JsonObject createJsonObject(String type, JsonArray coordinates) {
        JsonObject object = new JsonObject();
        object.addProperty("type", type);
        object.add("coordinates", coordinates);
        return object;
    }

    JsonArray getCoordinates(JsonElement element) {
        JsonObject object = element.getAsJsonObject();
        return object.getAsJsonArray("coordinates");
    }

    JsonArray serializePointToArray(SurrealPoint point) {
        return serializePointToArray(point.getLongitude(), point.getLatitude());
    }

    JsonArray serializePointToArray(double longitude, double latitude) {
        JsonArray pointArray = new JsonArray();
        pointArray.add(longitude);
        pointArray.add(latitude);
        return pointArray;
    }

    SurrealPoint deserializePointFromArray(JsonArray coordinates) {
        double longitude = coordinates.get(0).getAsDouble();
        double latitude = coordinates.get(1).getAsDouble();
        return SurrealPoint.fromLongitudeLatitude(longitude, latitude);
    }

    JsonArray serializeLineStringToArray(SurrealLineString lineString) {
        JsonArray lineStringArray = new JsonArray();
        for (SurrealPoint point : lineString.getPoints()) {
            lineStringArray.add(serializePointToArray(point));
        }
        return lineStringArray;
    }

    SurrealLineString deserializeLineStringFromArray(JsonArray coordinates) {
        List<SurrealPoint> points = new ArrayList<>(coordinates.size());
        for (JsonElement pointCoordinatesElement : coordinates) {
            points.add(deserializePointFromArray(pointCoordinatesElement.getAsJsonArray()));
        }
        return new SurrealLineString(ImmutableList.copyOf(points));
    }

    JsonArray serializePolygonToArray(SurrealPolygon polygon) {
        JsonArray coordinates = new JsonArray();
        coordinates.add(serializeLinearRing(polygon.getOuterRing()));

        Optional<ImmutableList<SurrealPoint>> innerRing = polygon.getInnerRing();
        innerRing.ifPresent(surrealPoints -> coordinates.add(serializeLinearRing(surrealPoints)));
        return coordinates;
    }

    JsonArray serializeLinearRing(ImmutableList<SurrealPoint> linearRing) {
        JsonArray array = new JsonArray();
        for (SurrealPoint point : linearRing) {
            array.add(serializePointToArray(point));
        }
        // Add the first point again to close the ring
        array.add(array.get(0));

        return array;
    }

    SurrealPolygon deserializePolygonFromArray(JsonArray coordinates) {
        ImmutableList<SurrealPoint> outerRing = deserializeLinearRing(coordinates.get(0).getAsJsonArray());
        ImmutableList<SurrealPoint> innerRing = null;
        if (coordinates.size() > 1) {
            innerRing = deserializeLinearRing(coordinates.get(1).getAsJsonArray());
        }
        return new SurrealPolygon(outerRing, innerRing);
    }

    ImmutableList<SurrealPoint> deserializeLinearRing(JsonElement element) {
        JsonArray pointCoordinates = element.getAsJsonArray();
        List<SurrealPoint> points = new ArrayList<>(pointCoordinates.size());

        // Ignore the last point, as it is the same as the first
        for (int index = 0; index < pointCoordinates.size() - 1; index++) {
            points.add(deserializePointFromArray(pointCoordinates.get(index).getAsJsonArray()));
        }

        return ImmutableList.copyOf(points);
    }
}
