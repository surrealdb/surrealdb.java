package com.surrealdb.connection.gson;

import com.google.gson.*;
import com.surrealdb.driver.geometry.MultiPoint;
import com.surrealdb.driver.geometry.Point;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

final class GeometryMultiPointAdaptor extends GeometryAdaptor<MultiPoint> {

    GeometryMultiPointAdaptor() {
        super(MultiPoint.class);
    }

    @Override
    public @NotNull JsonElement serialize(@NotNull MultiPoint src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray coordinates = new JsonArray();
        for (Point point : src) {
            coordinates.add(serializePoint(point));
        }

        return createJsonObject("MultiPoint", coordinates);
    }

    @Override
    public @NotNull MultiPoint deserialize(@NotNull JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray coordinates = getCoordinates(json);
        List<Point> points = new ArrayList<>(coordinates.size());

        for (JsonElement pointCoordinatesElement : coordinates) {
            points.add(deserializePoint(pointCoordinatesElement.getAsJsonArray()));
        }

        return MultiPoint.from(points);
    }
}
