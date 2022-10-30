package com.surrealdb.connection.gson.geometry;

import com.google.gson.*;
import com.surrealdb.driver.model.geometry.SurrealPoint;

import java.lang.reflect.Type;

public final class SurrealPointAdaptor extends SurrealGeometryAdaptor<SurrealPoint> {

    public SurrealPointAdaptor() {
        super(SurrealPoint.class);
    }

    @Override
    public JsonElement serialize(SurrealPoint src, Type typeOfSrc, JsonSerializationContext context) {
        return createJsonObject("Point", serializePointToArray(src));
    }

    @Override
    public SurrealPoint deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray coordinates = getCoordinates(json);
        return deserializePointFromArray(coordinates);
    }
}
