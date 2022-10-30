package com.surrealdb.connection.gson.geometry;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.surrealdb.driver.model.geometry.SurrealLineString;
import com.surrealdb.driver.model.geometry.SurrealMultiLineString;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public final class SurrealMultiLineStringAdaptor extends SurrealGeometryAdaptor<SurrealMultiLineString> {

    public SurrealMultiLineStringAdaptor() {
        super(SurrealMultiLineString.class);
    }

    @Override
    public JsonElement serialize(SurrealMultiLineString src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray coordinates = new JsonArray();
        for (SurrealLineString line : src.getLines()) {
            coordinates.add(serializeLineStringToArray(line));
        }

        return createJsonObject("MultiLineString", coordinates);
    }

    @Override
    public SurrealMultiLineString deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray coordinates = getCoordinates(json);
        List<SurrealLineString> lines = new ArrayList<>(coordinates.size());

        for (JsonElement lineCoordinatesElement : coordinates) {
            lines.add(deserializeLineStringFromArray(lineCoordinatesElement.getAsJsonArray()));
        }

        return new SurrealMultiLineString(ImmutableList.copyOf(lines));
    }
}
