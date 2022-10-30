package com.surrealdb.connection.gson.patch;

import com.google.gson.*;
import com.surrealdb.driver.model.patch.AddPatch;

import java.lang.reflect.Type;

public class SurrealAddPatchAdaptor extends SurrealPatchAdaptor<AddPatch> {

    public SurrealAddPatchAdaptor() {
        super(AddPatch.class, "add");
    }

    @Override
    public JsonElement serialize(AddPatch src, Type typeOfSrc, JsonSerializationContext context) {
        return createObject(src, src.getValue());
    }

    @Override
    public AddPatch deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        String path = object.get("path").getAsString();
        String value = object.get("value").getAsString();
        return new AddPatch(path, value);
    }
}
