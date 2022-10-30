package com.surrealdb.connection.gson.geometry;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.surrealdb.driver.model.geometry.SurrealMultiPoint;
import com.surrealdb.driver.model.geometry.SurrealPoint;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public final class SurrealMultiPointAdaptor extends SurrealGeometryAdaptor<SurrealMultiPoint> {

    public SurrealMultiPointAdaptor() {
        super(SurrealMultiPoint.class);
    }

    @Override
    public JsonElement serialize(SurrealMultiPoint src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray coordinates = new JsonArray();
        for (SurrealPoint point : src.getPoints()) {
            coordinates.add(serializePointToArray(point));
        }

        return createJsonObject("MultiPoint", coordinates);
    }

    @Override
    public SurrealMultiPoint deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray coordinates = getCoordinates(json);
        List<SurrealPoint> points = new ArrayList<>(coordinates.size());

        for (JsonElement pointCoordinatesElement : coordinates) {
            points.add(deserializePointFromArray(pointCoordinatesElement.getAsJsonArray()));
        }

        return new SurrealMultiPoint(ImmutableList.copyOf(points));
    }
}
