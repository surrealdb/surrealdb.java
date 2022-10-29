package com.surrealdb.connection.gson;

import com.google.gson.*;
import com.surrealdb.driver.model.patch.ChangePatch;

import java.lang.reflect.Type;

/**
 * @author Damian Kocher
 */
public final class SurrealChangePatchAdaptor implements SurrealGsonAdaptor<ChangePatch> {

    @Override
    public JsonElement serialize(ChangePatch src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("op", "change");
        object.addProperty("path", src.getPath());
        object.addProperty("value", src.getValue());
        return object;
    }

    @Override
    public ChangePatch deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return context.deserialize(json, ChangePatch.class);
    }

    @Override
    public Class<ChangePatch> getAdaptorClass() {
        return ChangePatch.class;
    }
}
