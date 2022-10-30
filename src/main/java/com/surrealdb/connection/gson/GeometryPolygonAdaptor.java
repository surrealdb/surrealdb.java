package com.surrealdb.connection.gson;

import com.google.gson.*;
import com.surrealdb.driver.model.geometry.Polygon;

import java.lang.reflect.Type;

final class GeometryPolygonAdaptor extends GeometryAdaptor<Polygon> {

    GeometryPolygonAdaptor() {
        super(Polygon.class);
    }

    @Override
    public JsonElement serialize(Polygon src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray coordinates = serializePolygonToArray(src);
        return createJsonObject("Polygon", coordinates);
    }

    @Override
    public Polygon deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray coordinates = getCoordinates(json);
        return deserializePolygonFromArray(coordinates);
    }
}
