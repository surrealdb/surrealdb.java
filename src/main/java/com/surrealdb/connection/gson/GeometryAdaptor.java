package com.surrealdb.connection.gson;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.surrealdb.driver.geometry.GeometryPrimitive;
import com.surrealdb.driver.geometry.Line;
import com.surrealdb.driver.geometry.Point;
import com.surrealdb.driver.geometry.Polygon;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

abstract class GeometryAdaptor<T extends GeometryPrimitive> extends SurrealGsonAdaptor<T> {

    GeometryAdaptor(Class<T> adaptorClass) {
        super(adaptorClass);
    }

    @NotNull JsonObject createJsonObject(String type, JsonArray coordinates) {
        JsonObject object = new JsonObject();
        object.addProperty("type", type);
        object.add("coordinates", coordinates);
        return object;
    }

    JsonArray getCoordinates(@NotNull JsonElement element) {
        JsonObject object = element.getAsJsonObject();
        return object.getAsJsonArray("coordinates");
    }

    @NotNull JsonArray serializePoint(@NotNull Point point) {
        JsonArray coordinates = new JsonArray();
        coordinates.add(point.getX());
        coordinates.add(point.getY());
        return coordinates;
    }

    @NotNull Point deserializePoint(@NotNull JsonArray coordinates) {
        double x = coordinates.get(0).getAsDouble();
        double y = coordinates.get(1).getAsDouble();
        return Point.fromXY(x, y);
    }

    @NotNull JsonArray serializeLine(@NotNull Line line) {
        JsonArray lineStringArray = new JsonArray();
        for (Point point : line) {
            lineStringArray.add(serializePoint(point));
        }
        return lineStringArray;
    }

    @NotNull Line deserializeLine(@NotNull JsonArray coordinates) {
        List<Point> points = new ArrayList<>(coordinates.size());
        for (JsonElement pointCoordinatesElement : coordinates) {
            points.add(deserializePoint(pointCoordinatesElement.getAsJsonArray()));
        }
        return Line.from(points);
    }

    @NotNull JsonArray serializePolygon(@NotNull Polygon polygon) {
        JsonArray coordinates = new JsonArray();

        Line exterior = polygon.getExterior();
        coordinates.add(serializeLine(exterior));

        ImmutableList<Line> interiors = polygon.getInteriors();
        for (Line interior : interiors) {
            coordinates.add(serializeLine(interior));
        }

        return coordinates;
    }

    @NotNull Polygon deserializePolygon(@NotNull JsonArray coordinates) {
        Line exterior = deserializeLine(coordinates.get(0).getAsJsonArray());
        List<Line> interiors = new ArrayList<>();
        // Interior rings start at index 1, as index 0 is the exterior ring
        for (int i = 1; i < coordinates.size(); i++) {
            interiors.add(deserializeLine(coordinates.get(i).getAsJsonArray()));
        }
        return Polygon.withInteriorPolygons(exterior, interiors);
    }
}
