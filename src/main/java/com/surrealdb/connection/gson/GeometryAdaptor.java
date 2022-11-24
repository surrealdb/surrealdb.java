package com.surrealdb.connection.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.surrealdb.driver.geometry.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

abstract class GeometryAdaptor<T extends GeometryPrimitive> extends SurrealGsonAdaptor<T> {

    GeometryAdaptor(@NotNull Class<T> adaptorClass) {
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

    @NotNull JsonArray serializeLine(@NotNull LineString line) {
        JsonArray lineStringArray = new JsonArray();
        for (Point point : line) {
            lineStringArray.add(serializePoint(point));
        }
        return lineStringArray;
    }

    @NotNull JsonArray serializeLinearRing(@NotNull LinearRing ring) {
        JsonArray linearRingCoords = new JsonArray();
        for (Point point : ring) {
            linearRingCoords.add(serializePoint(point));
        }
        return linearRingCoords;
    }

    @NotNull LineString deserializeLine(@NotNull JsonArray coordinates) {
        List<Point> points = new ArrayList<>(coordinates.size());
        for (JsonElement pointCoordinatesElement : coordinates) {
            points.add(deserializePoint(pointCoordinatesElement.getAsJsonArray()));
        }
        return LineString.from(points);
    }

    @NotNull JsonArray serializePolygon(@NotNull Polygon polygon) {
        JsonArray coordinates = new JsonArray();

        LinearRing exterior = polygon.getExterior();
        coordinates.add(serializeLinearRing(exterior));

        polygon.interiorIterator().forEachRemaining((interior) -> coordinates.add(serializeLinearRing(interior)));

        return coordinates;
    }

    @NotNull Polygon deserializePolygon(@NotNull JsonArray coordinates) {
        LinearRing exterior = deserializeLine(coordinates.get(0).getAsJsonArray()).toLinearRing();
        List<LinearRing> interiors = new ArrayList<>();
        // Interior rings start at index 1, as index 0 is the exterior ring
        for (int i = 1; i < coordinates.size(); i++) {
            LinearRing interior = deserializeLine(coordinates.get(i).getAsJsonArray()).toLinearRing();
            interiors.add(interior);
        }
        return Polygon.withInteriorPolygons(exterior, interiors);
    }
}
