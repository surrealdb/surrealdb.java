package com.surrealdb.gson;

import com.google.gson.*;
import com.surrealdb.geometry.Polygon;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

final class GeometryPolygonAdaptor extends GeometryAdaptor<Polygon> {

    GeometryPolygonAdaptor() {
        super(Polygon.class);
    }

    @Override
    public @NotNull JsonElement serialize(@NotNull Polygon polygon, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray coordinates = serializePolygon(polygon);
        return createJsonObject("Polygon", coordinates);
    }

    @Override
    public @NotNull Polygon deserialize(@NotNull JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray coordinates = getCoordinates(json);
        return deserializePolygon(coordinates);
    }
}
