package com.surrealdb.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.surrealdb.geometry.*;
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

    @NotNull JsonArray getCoordinates(@NotNull JsonElement element) {
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

    @NotNull JsonArray serializeIterableOfPointsIterable(@NotNull Iterable<? extends Iterable<? extends Point>> iterable) {
        JsonArray coordinates = new JsonArray();

        for (Iterable<? extends Point> points : iterable) {
            JsonArray pointsArray = new JsonArray();
            coordinates.add(pointsArray);

            for (Point point : points) {
                pointsArray.add(serializePoint(point));
            }
        }

        return coordinates;
    }

    @NotNull JsonArray serializePoints(@NotNull Iterable<Point> points) {
        JsonArray coordinates = new JsonArray();
        for (Point point : points) {
            coordinates.add(serializePoint(point));
        }
        return coordinates;
    }

    @NotNull List<Point> deserializePoints(@NotNull JsonArray coordinates) {
        List<Point> points = new ArrayList<>(coordinates.size());
        for (JsonElement pointCoordinatesElement : coordinates) {
            points.add(deserializePoint(pointCoordinatesElement.getAsJsonArray()));
        }
        return points;
    }

    @NotNull JsonArray serializePolygon(@NotNull Polygon polygon) {
        JsonArray coordinates = new JsonArray();

        LinearRing exterior = polygon.getExterior();
        coordinates.add(serializePoints(exterior));

        polygon.interiorIterator().forEachRemaining((interior) -> coordinates.add(serializePoints(interior)));

        return coordinates;
    }

    private @NotNull LinearRing deserializeLinearRing(@NotNull JsonElement element) {
        JsonArray coordinates = element.getAsJsonArray();
        List<Point> points = deserializePoints(coordinates);
        return LinearRing.from(points);
    }

    @NotNull Polygon deserializePolygon(@NotNull JsonArray coordinates) {
        LinearRing exterior = deserializeLinearRing(coordinates.get(0));

        List<LinearRing> interiors = new ArrayList<>();
        // Interior rings start at index 1, as index 0 is the exterior ring
        for (int i = 1; i < coordinates.size(); i++) {
            JsonElement serializedInterior = coordinates.get(i);
            LinearRing interior = deserializeLinearRing(serializedInterior);
            interiors.add(interior);
        }

        return Polygon.withInteriorPolygons(exterior, interiors);
    }
}
