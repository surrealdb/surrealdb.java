package com.surrealdb.connection.gson;

import com.google.gson.*;
import com.surrealdb.driver.model.patch.RemovePatch;

import java.lang.reflect.Type;

/**
 * @author Damian Kocher
 */
public final class SurrealRemovePatchAdaptor implements SurrealGsonAdaptor<RemovePatch> {

    @Override
    public JsonElement serialize(RemovePatch src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("op", "remove");
        object.addProperty("path", src.getPath());
        return object;
    }

    @Override
    public RemovePatch deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return context.deserialize(json, RemovePatch.class);
    }

    @Override
    public Class<RemovePatch> getAdaptorClass() {
        return RemovePatch.class;
    }
}
