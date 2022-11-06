package com.surrealdb.connection.gson;

import com.google.gson.*;
import com.surrealdb.driver.geometry.LineString;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

final class GeometryLineAdaptor extends GeometryAdaptor<LineString> {

    GeometryLineAdaptor() {
        super(LineString.class);
    }

    @Override
    public @NotNull JsonElement serialize(@NotNull LineString src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray coordinates = serializeLine(src);
        return createJsonObject("LineString", coordinates);
    }

    @Override
    public @NotNull LineString deserialize(@NotNull JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray coordinates = getCoordinates(json);
        return deserializeLine(coordinates);
    }
}
