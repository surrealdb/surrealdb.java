package com.surrealdb.connection.gson;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.surrealdb.driver.geometry.MultiPolygon;
import com.surrealdb.driver.geometry.Polygon;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

final class GeometryMultiPolygonAdaptor extends GeometryAdaptor<MultiPolygon> {

    GeometryMultiPolygonAdaptor() {
        super(MultiPolygon.class);
    }

    @Override
    public JsonElement serialize(MultiPolygon src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray coordinates = new JsonArray();
        for (Polygon polygon : src.getPolygons()) {
            coordinates.add(serializePolygon(polygon));
        }
        return createJsonObject("MultiPolygon", coordinates);
    }

    @Override
    public MultiPolygon deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray coordinates = getCoordinates(json);

        List<Polygon> polygons = new ArrayList<>(coordinates.size());
        for (JsonElement polygon : coordinates) {
            polygons.add(deserializePolygon(polygon.getAsJsonArray()));
        }

        return MultiPolygon.from(ImmutableList.copyOf(polygons));
    }
}
