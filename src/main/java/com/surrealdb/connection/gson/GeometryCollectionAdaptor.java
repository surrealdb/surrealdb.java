package com.surrealdb.connection.gson;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import com.surrealdb.driver.model.geometry.SurrealGeometryCollection;
import com.surrealdb.driver.model.geometry.SurrealGeometryPrimitive;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

class GeometryCollectionAdaptor extends SurrealGsonAdaptor<SurrealGeometryCollection> {

    GeometryCollectionAdaptor() {
        super(SurrealGeometryCollection.class);
    }

    @Override
    public JsonElement serialize(SurrealGeometryCollection src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("type", "GeometryCollection");

        JsonArray geometries = new JsonArray();
        object.add("geometries", geometries);

        for (SurrealGeometryPrimitive geometry : src.getGeometries()) {
            geometries.add(context.serialize(geometry));
        }

        return object;
    }

    @Override
    public SurrealGeometryCollection deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        JsonArray geometries = object.getAsJsonArray("geometries");

        List<SurrealGeometryPrimitive> geometryList = new ArrayList<>(geometries.size());
        for (JsonElement geometry : geometries) {
            SurrealGeometryPrimitive primitive = context.deserialize(geometry, SurrealGeometryPrimitive.class);
            geometryList.add(primitive);
        }

        return new SurrealGeometryCollection(ImmutableList.copyOf(geometryList));
    }
}
