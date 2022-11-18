package com.surrealdb.connection.gson;

import com.google.gson.*;
import com.surrealdb.driver.geometry.GeometryCollection;
import com.surrealdb.driver.geometry.GeometryPrimitive;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

class GeometryCollectionAdaptor extends SurrealGsonAdaptor<GeometryCollection> {

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
            GeometryPrimitive primitive = context.deserialize(geometry, GeometryPrimitive.class);
            geometryList.add(primitive);
        }

        return GeometryCollection.from(geometryList);
    }
}
