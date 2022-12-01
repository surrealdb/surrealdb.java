package com.surrealdb.gson;

import com.google.gson.*;
import com.surrealdb.geometry.Point;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

final class GeometryPointAdaptor extends GeometryAdaptor<Point> {

    GeometryPointAdaptor() {
        super(Point.class);
    }

    @Override
    public @NotNull JsonElement serialize(@NotNull Point point, Type typeOfSrc, JsonSerializationContext context) {
        return createJsonObject("Point", serializePoint(point));
    }

    @Override
    public @NotNull Point deserialize(@NotNull JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray coordinates = getCoordinates(json);
        return deserializePoint(coordinates);
    }
}