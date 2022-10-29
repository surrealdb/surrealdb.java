package com.surrealdb.connection.gson;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.Instant;

/**
 * @author Damian Kocher
 */
public final class SurrealInstantAdaptor implements SurrealGsonAdaptor<Instant> {

    @Override
    public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }

    @Override
    public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return Instant.parse(json.getAsString());
    }

    @Override
    public Class<Instant> getAdaptorClass() {
        return Instant.class;
    }
}
