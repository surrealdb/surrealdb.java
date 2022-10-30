package com.surrealdb.connection.gson;

import com.google.gson.*;
import com.surrealdb.driver.model.geometry.SurrealPolygon;

import java.lang.reflect.Type;

final class GeometryPolygonAdaptor extends GeometryAdaptor<SurrealPolygon> {

    GeometryPolygonAdaptor() {
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
}
