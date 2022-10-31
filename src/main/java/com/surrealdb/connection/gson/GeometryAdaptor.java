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

    JsonArray serializePoint(Point point) {
        JsonArray coordinates = new JsonArray();
        coordinates.add(point.getLongitude());
        coordinates.add(point.getLatitude());
        return coordinates;
    }

    Point deserializePoint(JsonArray coordinates) {
        double longitude = coordinates.get(0).getAsDouble();
        double latitude = coordinates.get(1).getAsDouble();
        return Point.fromLongitudeLatitude(longitude, latitude);
    }

    JsonArray serializeLine(Line line) {
        JsonArray lineStringArray = new JsonArray();
        for (Point point : line.getPoints()) {
            lineStringArray.add(serializePoint(point));
        }
        return lineStringArray;
    }

    Line deserializeLine(JsonArray coordinates) {
        List<Point> points = new ArrayList<>(coordinates.size());
        for (JsonElement pointCoordinatesElement : coordinates) {
            points.add(deserializePoint(pointCoordinatesElement.getAsJsonArray()));
        }
        return Line.from(points);
    }

    JsonArray serializePolygon(Polygon polygon) {
        JsonArray coordinates = new JsonArray();

        Line exterior = polygon.getExterior();
        coordinates.add(serializeLine(exterior));

        ImmutableList<Line> interiors = polygon.getInteriors();
        for (Line interior : interiors) {
            coordinates.add(serializeLine(interior));
        }

        return coordinates;
    }

    Polygon deserializePolygon(JsonArray coordinates) {
        Line exterior = deserializeLine(coordinates.get(0).getAsJsonArray());
        List<Line> interiors = new ArrayList<>();
        // Interior rings start at index 1, as index 0 is the exterior ring
        for (int i = 1; i < coordinates.size(); i++) {
            interiors.add(deserializeLine(coordinates.get(i).getAsJsonArray()));
        }
        return Polygon.withInteriorPolygons(exterior, interiors);
    }
}
