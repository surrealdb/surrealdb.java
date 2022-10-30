package com.surrealdb.connection.gson;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.surrealdb.driver.model.geometry.SurrealMultiPolygon;
import com.surrealdb.driver.model.geometry.SurrealPolygon;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

final class GeometryMultiPolygonAdaptor extends GeometryAdaptor<SurrealMultiPolygon> {

    GeometryMultiPolygonAdaptor() {
        super(SurrealMultiPolygon.class);
    }

    @Override
    public JsonElement serialize(SurrealMultiPolygon src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray coordinates = new JsonArray();
        for (SurrealPolygon polygon : src.getPolygons()) {
            coordinates.add(serializePolygonToArray(polygon));
        }
        return createJsonObject("MultiPolygon", coordinates);
    }

    @Override
    public SurrealMultiPolygon deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray coordinates = getCoordinates(json);

        List<SurrealPolygon> polygons = new ArrayList<>(coordinates.size());
        for (JsonElement polygon : coordinates) {
            polygons.add(deserializePolygonFromArray(polygon.getAsJsonArray()));
        }

        return new SurrealMultiPolygon(ImmutableList.copyOf(polygons));
    }
}
