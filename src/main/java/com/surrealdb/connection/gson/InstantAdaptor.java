package com.surrealdb.connection.gson;

import com.google.gson.*;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.time.Instant;

/**
 * @author Damian Kocher
 */
final class InstantAdaptor extends SurrealGsonAdaptor<Instant> {

    InstantAdaptor() {
        super(Instant.class);
    }

    @Override
    public @NotNull JsonElement serialize(@NotNull Instant src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.toString());
    }

    @Override
    public Instant deserialize(@NotNull JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return Instant.parse(json.getAsString());
    }
}
