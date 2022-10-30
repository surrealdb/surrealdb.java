package com.surrealdb.connection.gson;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.Instant;

/**
 * @author Damian Kocher
 */
public final class SurrealInstantAdaptor extends SurrealGsonAdaptor<Instant> {

    public SurrealInstantAdaptor() {
        super(Instant.class);
    }

    @Override
    public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }

    @Override
    public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return Instant.parse(json.getAsString());
    }
}
