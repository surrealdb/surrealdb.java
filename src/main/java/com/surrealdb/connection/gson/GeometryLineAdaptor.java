package com.surrealdb.connection.gson;

import com.google.gson.*;
import com.surrealdb.driver.geometry.Line;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

final class GeometryLineAdaptor extends GeometryAdaptor<Line> {

    GeometryLineAdaptor() {
        super(Line.class);
    }

    @Override
    public @NotNull JsonElement serialize(@NotNull Line src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray coordinates = serializeLine(src);
        return createJsonObject("LineString", coordinates);
    }

    @Override
    public @NotNull Line deserialize(@NotNull JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray coordinates = getCoordinates(json);
        return deserializeLine(coordinates);
    }
}
