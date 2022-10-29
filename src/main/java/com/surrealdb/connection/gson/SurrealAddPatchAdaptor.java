package com.surrealdb.connection.gson;

import com.google.gson.*;
import com.surrealdb.driver.model.patch.AddPatch;

import java.lang.reflect.Type;

/**
 * @author Damian Kocher
 */
public final class SurrealAddPatchAdaptor implements SurrealGsonAdaptor<AddPatch> {

    @Override
    public JsonElement serialize(AddPatch src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("type", "add");
        object.addProperty("path", src.getPath());
        object.addProperty("value", src.getValue());
        return object;
    }

    @Override
    public AddPatch deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return context.deserialize(json, AddPatch.class);
    }

    @Override
    public Class<AddPatch> getAdaptorClass() {
        return AddPatch.class;
    }
}
