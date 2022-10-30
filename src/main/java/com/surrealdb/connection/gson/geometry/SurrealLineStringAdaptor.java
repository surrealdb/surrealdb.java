package com.surrealdb.connection.gson.geometry;

import com.google.gson.*;
import com.surrealdb.driver.model.geometry.SurrealLineString;

import java.lang.reflect.Type;

public final class SurrealLineStringAdaptor extends SurrealGeometryAdaptor<SurrealLineString> {

    public SurrealLineStringAdaptor() {
        super(SurrealLineString.class);
    }

    @Override
    public JsonElement serialize(SurrealLineString src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray coordinates = serializeLineStringToArray(src);
        return createJsonObject("LineString", coordinates);
    }

    @Override
    public SurrealLineString deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray coordinates = getCoordinates(json);
        return deserializeLineStringFromArray(coordinates);
    }
}
