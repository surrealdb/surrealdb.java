package com.surrealdb.connection.gson;

import com.google.gson.*;
import com.surrealdb.driver.model.geometry.Point;

import java.lang.reflect.Type;

final class GeometryPointAdaptor extends GeometryAdaptor<Point> {

    GeometryPointAdaptor() {
        super(Point.class);
    }

    @Override
    public JsonElement serialize(Point src, Type typeOfSrc, JsonSerializationContext context) {
        return createJsonObject("Point", serializePoint(src));
    }

    @Override
    public Point deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray coordinates = getCoordinates(json);
        return deserializePoint(coordinates);
    }
}
