package com.surrealdb.connection.gson.patch;

import com.google.gson.*;
import com.surrealdb.driver.model.patch.RemovePatch;

import java.lang.reflect.Type;

public class SurrealRemovePatchAdaptor extends SurrealPatchAdaptor<RemovePatch> {

    public SurrealRemovePatchAdaptor() {
        super(RemovePatch.class, "remove");
    }

    @Override
    public JsonElement serialize(RemovePatch src, Type typeOfSrc, JsonSerializationContext context) {
        return createObject(src, null);
    }

    @Override
    public RemovePatch deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        String path = object.get("path").getAsString();
        return new RemovePatch(path);
    }
}
