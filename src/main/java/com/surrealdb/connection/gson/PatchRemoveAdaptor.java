package com.surrealdb.connection.gson;

import com.google.gson.*;
import com.surrealdb.driver.model.patch.RemovePatch;

import java.lang.reflect.Type;

final class PatchRemoveAdaptor extends PatchAdaptor<RemovePatch> {

    PatchRemoveAdaptor() {
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
