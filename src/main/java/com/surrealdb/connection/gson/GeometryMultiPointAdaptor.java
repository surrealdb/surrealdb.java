package com.surrealdb.connection.gson;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.surrealdb.driver.model.geometry.MultiPoint;
import com.surrealdb.driver.model.geometry.Point;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

final class GeometryMultiPointAdaptor extends GeometryAdaptor<MultiPoint> {

    GeometryMultiPointAdaptor() {
        super(MultiPoint.class);
    }

    @Override
    public JsonElement serialize(MultiPoint src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray coordinates = new JsonArray();
        for (Point point : src.getPoints()) {
            coordinates.add(serializePointToArray(point));
        }

        return createJsonObject("MultiPoint", coordinates);
    }

    @Override
    public MultiPoint deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray coordinates = getCoordinates(json);
        List<Point> points = new ArrayList<>(coordinates.size());

        for (JsonElement pointCoordinatesElement : coordinates) {
            points.add(deserializePointFromArray(pointCoordinatesElement.getAsJsonArray()));
        }

        return new MultiPoint(ImmutableList.copyOf(points));
    }
}
