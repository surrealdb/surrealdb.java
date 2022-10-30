package com.surrealdb.connection.gson.patch;

import com.google.gson.*;
import com.surrealdb.driver.model.patch.ReplacePatch;

import java.lang.reflect.Type;

public final class SurrealReplacePatchAdaptor extends SurrealPatchAdaptor<ReplacePatch> {

    public SurrealReplacePatchAdaptor() {
        super(ReplacePatch.class, "replace");
    }

    @Override
    public JsonElement serialize(ReplacePatch src, Type typeOfSrc, JsonSerializationContext context) {
        return createObject(src, src.getValue());
    }

    @Override
    public ReplacePatch deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        String path = object.get("path").getAsString();
        String value = object.get("value").getAsString();
        return new ReplacePatch(path, value);
    }
}
