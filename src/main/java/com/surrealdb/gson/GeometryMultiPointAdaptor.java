package com.surrealdb.gson;

import com.google.gson.*;
import com.surrealdb.geometry.MultiPoint;
import com.surrealdb.geometry.Point;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.List;

final class GeometryMultiPointAdaptor extends GeometryAdaptor<MultiPoint> {

    GeometryMultiPointAdaptor() {
        super(MultiPoint.class);
    }

    @Override
    public @NotNull JsonElement serialize(@NotNull MultiPoint multiPoint, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray coordinates = serializePoints(multiPoint);
        return createJsonObject("MultiPoint", coordinates);
    }

    @Override
    public @NotNull MultiPoint deserialize(@NotNull JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray coordinates = getCoordinates(json);
        List<Point> points = deserializePoints(coordinates);
        return MultiPoint.from(points);
    }
}
