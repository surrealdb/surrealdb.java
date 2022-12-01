package com.surrealdb.gson;

import com.google.gson.*;
import com.surrealdb.geometry.LinearRing;
import com.surrealdb.geometry.Point;

import java.lang.reflect.Type;
import java.util.List;

final class GeometryLinearRingAdaptor extends GeometryAdaptor<LinearRing> {

    public GeometryLinearRingAdaptor() {
        super(LinearRing.class);
    }

    @Override
    public LinearRing deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray coordinates = getCoordinates(json);
        List<Point> points = deserializePoints(coordinates);
        return LinearRing.from(points);
    }

    @Override
    public JsonElement serialize(LinearRing linearRing, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray coordinates = serializePoints(linearRing);
        return createJsonObject("LineString", coordinates);
    }
}
