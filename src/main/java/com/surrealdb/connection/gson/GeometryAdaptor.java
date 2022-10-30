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
import java.util.Optional;

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
        return new Line(ImmutableList.copyOf(points));
    }

    JsonArray serializePolygonToArray(Polygon polygon) {
        JsonArray coordinates = new JsonArray();
        coordinates.add(serializeLinearRing(polygon.getOuterRing()));

        Optional<ImmutableList<Point>> innerRing = polygon.getInnerRing();
        innerRing.ifPresent(surrealPoints -> coordinates.add(serializeLinearRing(surrealPoints)));
        return coordinates;
    }

    JsonArray serializeLinearRing(ImmutableList<Point> linearRing) {
        JsonArray array = new JsonArray();
        for (Point point : linearRing) {
            array.add(serializePointToArray(point));
        }
        // Add the first point again to close the ring
        array.add(array.get(0));

        return array;
    }

    Polygon deserializePolygonFromArray(JsonArray coordinates) {
        ImmutableList<Point> outerRing = deserializeLinearRing(coordinates.get(0).getAsJsonArray());
        ImmutableList<Point> innerRing = null;
        if (coordinates.size() > 1) {
            innerRing = deserializeLinearRing(coordinates.get(1).getAsJsonArray());
        }
        return new Polygon(outerRing, innerRing);
    }

    ImmutableList<Point> deserializeLinearRing(JsonElement element) {
        JsonArray pointCoordinates = element.getAsJsonArray();
        List<Point> points = new ArrayList<>(pointCoordinates.size());

        // Ignore the last point, as it is the same as the first
        for (int index = 0; index < pointCoordinates.size() - 1; index++) {
            points.add(deserializePointFromArray(pointCoordinates.get(index).getAsJsonArray()));
        }

        return ImmutableList.copyOf(points);
    }
}
