package com.surrealdb.gson;

import com.google.gson.*;
import com.surrealdb.geometry.MultiPolygon;
import com.surrealdb.geometry.Polygon;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

final class GeometryMultiPolygonAdaptor extends GeometryAdaptor<MultiPolygon> {

    GeometryMultiPolygonAdaptor() {
        super(MultiPolygon.class);
    }

    @Override
    public @NotNull JsonElement serialize(@NotNull MultiPolygon multiPolygon, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray coordinates = new JsonArray();
        for (Polygon polygon : multiPolygon) {
            coordinates.add(serializePolygon(polygon));
        }
        return createJsonObject("MultiPolygon", coordinates);
    }

    @Override
    public @NotNull MultiPolygon deserialize(@NotNull JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray coordinates = getCoordinates(json);

        List<Polygon> polygons = new ArrayList<>(coordinates.size());
        for (JsonElement polygon : coordinates) {
            JsonArray polygonCoordinates = polygon.getAsJsonArray();
            polygons.add(deserializePolygon(polygonCoordinates));
        }

        return MultiPolygon.from(polygons);
    }
}
