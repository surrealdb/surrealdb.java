package com.surrealdb.connection.gson.patch;

import com.google.gson.*;
import com.surrealdb.driver.model.patch.ChangePatch;

import java.lang.reflect.Type;

public class SurrealChangePatchAdaptor extends SurrealPatchAdaptor<ChangePatch> {

    public SurrealChangePatchAdaptor() {
        super(ChangePatch.class, "change");
    }

    @Override
    public JsonElement serialize(ChangePatch src, Type typeOfSrc, JsonSerializationContext context) {
        return createObject(src, src.getValue());
    }

    @Override
    public ChangePatch deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        String path = object.get("path").getAsString();
        String value = object.get("value").getAsString();
        return new ChangePatch(path, value);
    }
}
