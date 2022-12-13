package com.surrealdb.gson;

import com.google.gson.*;
import com.surrealdb.geometry.LineString;
import com.surrealdb.geometry.MultiLineString;
import com.surrealdb.geometry.Point;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

final class GeometryMultiLineStringAdaptor extends GeometryAdaptor<MultiLineString> {

    GeometryMultiLineStringAdaptor() {
        super(MultiLineString.class);
    }

    @Override
    public @NotNull JsonElement serialize(@NotNull MultiLineString multiLineString, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray coordinates = serializeIterableOfPointsIterable(multiLineString);
        return createJsonObject("MultiLineString", coordinates);
    }

    @Override
    public @NotNull MultiLineString deserialize(@NotNull JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray coordinates = getCoordinates(json);
        List<LineString> lines = new ArrayList<>(coordinates.size());

        for (JsonElement lineString : coordinates) {
            JsonArray lineStringCoordinates = lineString.getAsJsonArray();
            List<Point> points = deserializePoints(lineStringCoordinates);
            lines.add(LineString.from(points));
        }

        return MultiLineString.from(lines);
    }
}
