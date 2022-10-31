package com.surrealdb.connection.gson;

import com.google.gson.*;
import com.surrealdb.driver.model.geometry.Line;

import java.lang.reflect.Type;

final class GeometryLineAdaptor extends GeometryAdaptor<Line> {

    GeometryLineAdaptor() {
        super(Line.class);
    }

    @Override
    public JsonElement serialize(Line src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray coordinates = serializeLine(src);
        return createJsonObject("LineString", coordinates);
    }

    @Override
    public Line deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray coordinates = getCoordinates(json);
        return deserializeLine(coordinates);
    }
}
