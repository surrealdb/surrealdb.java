package com.surrealdb.connection.gson;

import com.google.gson.*;
import com.surrealdb.driver.geometry.Point;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

final class GeometryPointAdaptor extends GeometryAdaptor<Point> {

    GeometryPointAdaptor() {
        super(Point.class);
    }

    @Override
    public @NotNull JsonElement serialize(@NotNull Point src, Type typeOfSrc, JsonSerializationContext context) {
        return createJsonObject("Point", serializePoint(src));
    }

    @Override
    public @NotNull Point deserialize(@NotNull JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray coordinates = getCoordinates(json);
        return deserializePoint(coordinates);
    }
}
