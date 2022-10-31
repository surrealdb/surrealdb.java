package com.surrealdb.connection.gson;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.surrealdb.driver.model.geometry.GeometryPrimitive;
import com.surrealdb.driver.model.geometry.Line;
import com.surrealdb.driver.model.geometry.Point;
import com.surrealdb.driver.model.geometry.Polygon;

import java.util.ArrayList;
import java.util.List;

abstract class GeometryAdaptor<T extends GeometryPrimitive> extends SurrealGsonAdaptor<T> {

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

    JsonArray serializePointToArray(Point point) {
        return serializePointToArray(point.getLongitude(), point.getLatitude());
    }

    JsonArray serializePointToArray(double longitude, double latitude) {
        JsonArray pointArray = new JsonArray();
        pointArray.add(longitude);
        pointArray.add(latitude);
        return pointArray;
    }

    Point deserializePointFromArray(JsonArray coordinates) {
        double longitude = coordinates.get(0).getAsDouble();
        double latitude = coordinates.get(1).getAsDouble();
        return Point.fromLongitudeLatitude(longitude, latitude);
    }

    JsonArray serializeLineStringToArray(Line line) {
        JsonArray lineStringArray = new JsonArray();
        for (Point point : line.getPoints()) {
            lineStringArray.add(serializePointToArray(point));
        }
        return lineStringArray;
    }

    Line deserializeLineStringFromArray(JsonArray coordinates) {
        List<Point> points = new ArrayList<>(coordinates.size());
        for (JsonElement pointCoordinatesElement : coordinates) {
            points.add(deserializePointFromArray(pointCoordinatesElement.getAsJsonArray()));
        }
        return Line.fromPoints(points);
    }

    JsonArray serializePolygonToArray(Polygon polygon) {
        ImmutableList<Point> outerRing = polygon.getOuterRing();
        ImmutableList<Point> innerRing = polygon.getInnerRing();
        JsonArray coordinates = new JsonArray();

        // Outer ring will always be present
        coordinates.add(serializeLinearRing(outerRing));

        // Inner ring is optional
        if (!innerRing.isEmpty()) {
            coordinates.add(serializeLinearRing(innerRing));
        }

        return coordinates;
    }

    JsonArray serializeLinearRing(ImmutableList<Point> linearRing) {
        JsonArray array = new JsonArray();
        for (Point point : linearRing) {
            array.add(serializePointToArray(point));
        }

        return array;
    }

    Polygon deserializePolygonFromArray(JsonArray coordinates) {
        ImmutableList<Point> outerRing = deserializeLinearRing(coordinates.get(0).getAsJsonArray());
        ImmutableList<Point> innerRing = null;
        if (coordinates.size() > 1) {
            innerRing = deserializeLinearRing(coordinates.get(1).getAsJsonArray());
        }
        return Polygon.fromOuterAndInnerRing(outerRing, innerRing);
    }

    ImmutableList<Point> deserializeLinearRing(JsonElement element) {
        JsonArray pointCoordinates = element.getAsJsonArray();
        List<Point> points = new ArrayList<>(pointCoordinates.size());

        for (JsonElement pointCoordinate : pointCoordinates) {
            points.add(deserializePointFromArray(pointCoordinate.getAsJsonArray()));
        }

        return ImmutableList.copyOf(points);
    }
}
