package com.surrealdb.connection.gson;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.surrealdb.driver.model.geometry.SurrealLineString;
import com.surrealdb.driver.model.geometry.SurrealPoint;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SurrealLineStringAdaptor extends SurrealGeometryAdaptor<SurrealLineString> {

    public SurrealLineStringAdaptor() {
        super(SurrealLineString.class);
    }

    @Override
    public JsonElement serialize(SurrealLineString src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray coordinates = new JsonArray();

        for (SurrealPoint point : src.getPoints()) {
            coordinates.add(serializePointToArray(point));
        }

        return createJsonObject("LineString", coordinates);
    }

    @Override
    public SurrealLineString deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray coordinates = getCoordinates(json);
        List<SurrealPoint> points = new ArrayList<>(coordinates.size());

        for (JsonElement pointCoordinatesElement : coordinates) {
            points.add(deserializePointFromArray(pointCoordinatesElement.getAsJsonArray()));
        }

        return new SurrealLineString(ImmutableList.copyOf(points));
    }
}
