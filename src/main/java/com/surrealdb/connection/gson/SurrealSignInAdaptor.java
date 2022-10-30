package com.surrealdb.connection.gson;

import com.google.gson.*;
import com.surrealdb.driver.model.SignIn;

import java.lang.reflect.Type;

public final class SurrealSignInAdaptor extends SurrealGsonAdaptor<SignIn> {

    public SurrealSignInAdaptor() {
        super(SignIn.class);
    }

    @Override
    public JsonElement serialize(SignIn src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("user", src.getUser());
        object.addProperty("pass", src.getPass());
        src.getNamespace().ifPresent(namespace -> object.addProperty("NS", namespace));
        src.getDatabase().ifPresent(database -> object.addProperty("DB", database));
        src.getScope().ifPresent(scope -> object.addProperty("SC", scope));

        return object;
    }

    @Override
    public SignIn deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        return new SignIn(
            object.get("user").getAsString(),
            object.get("pass").getAsString(),
            object.has("NS") ? object.get("NS").getAsString() : null,
            object.has("DB") ? object.get("DB").getAsString() : null,
            object.has("SC") ? object.get("SC").getAsString() : null
        );
    }
}
