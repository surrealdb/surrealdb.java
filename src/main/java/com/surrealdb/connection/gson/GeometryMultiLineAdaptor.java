package com.surrealdb.connection.gson;

import com.google.gson.*;
import com.surrealdb.driver.geometry.Line;
import com.surrealdb.driver.geometry.MultiLine;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

final class GeometryMultiLineAdaptor extends GeometryAdaptor<MultiLine> {

    GeometryMultiLineAdaptor() {
        super(MultiLine.class);
    }

    @Override
    public @NotNull JsonElement serialize(@NotNull MultiLine src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray coordinates = new JsonArray();
        for (Line line : src.getLines()) {
            coordinates.add(serializeLine(line));
        }

        return createJsonObject("MultiLineString", coordinates);
    }

    @Override
    public @NotNull MultiLine deserialize(@NotNull JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray coordinates = getCoordinates(json);
        List<Line> lines = new ArrayList<>(coordinates.size());

        for (JsonElement lineCoordinatesElement : coordinates) {
            lines.add(deserializeLine(lineCoordinatesElement.getAsJsonArray()));
        }

        return MultiLine.from(lines);
    }
}
