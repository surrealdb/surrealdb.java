package com.surrealdb.gson;

import com.google.gson.*;
import com.surrealdb.geometry.LineString;
import com.surrealdb.geometry.Point;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.List;

final class GeometryLineStringAdaptor extends GeometryAdaptor<LineString> {

    GeometryLineStringAdaptor() {
        super(LineString.class);
    }

    @Override
    public @NotNull JsonElement serialize(@NotNull LineString lineString, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray coordinates = serializePoints(lineString);
        return createJsonObject("LineString", coordinates);
    }

    @Override
    public @NotNull LineString deserialize(@NotNull JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray coordinates = getCoordinates(json);
        List<Point> points = deserializePoints(coordinates);
        return LineString.from(points);
    }
}
