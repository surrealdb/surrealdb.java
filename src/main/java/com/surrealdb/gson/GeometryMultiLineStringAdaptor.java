package com.surrealdb.gson;

import com.google.gson.*;
import com.surrealdb.geometry.LineString;
import com.surrealdb.geometry.MultiLineString;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

final class GeometryMultiLineStringAdaptor extends GeometryAdaptor<MultiLineString> {

    GeometryMultiLineStringAdaptor() {
        super(MultiLineString.class);
    }

    @Override
    public @NotNull JsonElement serialize(@NotNull MultiLineString src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray coordinates = new JsonArray();
        for (LineString line : src) {
            coordinates.add(serializeLine(line));
        }

        return createJsonObject("MultiLineString", coordinates);
    }

    @Override
    public @NotNull MultiLineString deserialize(@NotNull JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray coordinates = getCoordinates(json);
        List<LineString> lines = new ArrayList<>(coordinates.size());

        for (JsonElement lineCoordinatesElement : coordinates) {
            lines.add(deserializeLine(lineCoordinatesElement.getAsJsonArray()));
        }

        return MultiLineString.from(lines);
    }
}
