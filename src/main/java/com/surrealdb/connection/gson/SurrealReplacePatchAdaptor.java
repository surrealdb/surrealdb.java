package com.surrealdb.connection.gson;

import com.google.gson.*;
import com.surrealdb.driver.model.patch.ReplacePatch;

import java.lang.reflect.Type;

/**
 * @author Damian Kocher
 */
public final class SurrealReplacePatchAdaptor implements SurrealGsonAdaptor<ReplacePatch> {

    @Override
    public JsonElement serialize(ReplacePatch src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("op", "replace");
        object.addProperty("path", src.getPath());
        object.addProperty("value", src.getValue());
        return object;
    }

    @Override
    public ReplacePatch deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return context.deserialize(json, ReplacePatch.class);
    }

    @Override
    public Class<ReplacePatch> getAdaptorClass() {
        return ReplacePatch.class;
    }
}
