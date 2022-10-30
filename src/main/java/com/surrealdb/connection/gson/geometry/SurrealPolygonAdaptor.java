package com.surrealdb.connection.gson.geometry;

import com.google.gson.*;
import com.surrealdb.driver.model.geometry.SurrealPolygon;

import java.lang.reflect.Type;

public class SurrealPolygonAdaptor extends SurrealGeometryAdaptor<SurrealPolygon> {

    public SurrealPolygonAdaptor() {
        super(SurrealPolygon.class);
    }

    @Override
    public JsonElement serialize(SurrealPolygon src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray coordinates = serializePolygonToArray(src);
        return createJsonObject("Polygon", coordinates);
    }

    @Override
    public SurrealPolygon deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray coordinates = getCoordinates(json);
        return deserializePolygonFromArray(coordinates);
    }

    @Override
    public Class<SurrealPolygon> getAdaptorClass() {
        return SurrealPolygon.class;
    }
}
