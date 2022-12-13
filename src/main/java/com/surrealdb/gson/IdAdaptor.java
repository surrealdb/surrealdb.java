package com.surrealdb.gson;

import com.google.gson.*;
import com.surrealdb.types.Id;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

final class IdAdaptor extends SurrealGsonAdaptor<Id> {

    IdAdaptor() {
        super(Id.class);
    }

    @Override
    public JsonElement serialize(@NotNull Id id, Type typeOfSrc, JsonSerializationContext context) {
        String combinedId = id.toCombinedId();
        return new JsonPrimitive(combinedId);
    }

    @Override
    public @NotNull Id deserialize(@NotNull JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String combinedId = json.getAsString();
        return Id.parse(combinedId);
    }
}
