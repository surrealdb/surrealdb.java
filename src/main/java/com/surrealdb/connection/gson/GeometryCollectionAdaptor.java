package com.surrealdb.connection.gson;

import com.google.gson.*;
import com.surrealdb.driver.geometry.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

final class GeometryCollectionAdaptor extends SurrealGsonAdaptor<GeometryCollection> {

    GeometryCollectionAdaptor() {
        super(GeometryCollection.class);
    }

    @Override
    public @NotNull JsonElement serialize(@NotNull GeometryCollection src, Type typeOfSrc, @NotNull JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("type", "GeometryCollection");

        JsonArray geometries = new JsonArray();
        object.add("geometries", geometries);

        for (GeometryPrimitive geometry : src) {
            geometries.add(context.serialize(geometry));
        }

        return object;
    }

    @Override
    public @NotNull GeometryCollection deserialize(@NotNull JsonElement json, Type typeOfT, @NotNull JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        JsonArray geometries = object.getAsJsonArray("geometries");

        List<GeometryPrimitive> geometryList = new ArrayList<>(geometries.size());
        for (JsonElement geometry : geometries) {
            String type = geometry.getAsJsonObject().get("type").getAsString();
            Class<? extends GeometryPrimitive> geometryClass = switch (type) {
                case "Point" -> Point.class;
                case "MultiPoint" -> MultiPoint.class;
                case "LineString" -> LineString.class;
                case "MultiLineString" -> MultiLineString.class;
                case "Polygon" -> Polygon.class;
                case "MultiPolygon" -> MultiPolygon.class;
                default -> throw new JsonParseException("Unknown geometry type: " + type);
            };

            geometryList.add(context.deserialize(geometry, geometryClass));
        }

        return GeometryCollection.from(geometryList);
    }
}
