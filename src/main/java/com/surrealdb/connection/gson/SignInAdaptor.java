package com.surrealdb.connection.gson;

import com.google.gson.*;
import com.surrealdb.driver.model.SignIn;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

final class SignInAdaptor extends SurrealGsonAdaptor<SignIn> {

    public static final String USER_KEY = "user";
    public static final String PASS_KEY = "pass";
    public static final String NAMESPACE_KEY = "NS";
    public static final String DATABASE_KEY = "DB";
    public static final String SCOPE_KEY = "SC";

    SignInAdaptor() {
        super(SignIn.class);
    }

    @Override
    public JsonElement serialize(SignIn src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty(USER_KEY, src.getUser());
        object.addProperty(PASS_KEY, src.getPassword());
        src.getNamespace().ifPresent(namespace -> object.addProperty(NAMESPACE_KEY, namespace));
        src.getDatabase().ifPresent(database -> object.addProperty(DATABASE_KEY, database));
        src.getScope().ifPresent(scope -> object.addProperty(SCOPE_KEY, scope));
        return object;
    }

    @Override
    public SignIn deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        return new SignIn(
            object.get(USER_KEY).getAsString(),
            object.get(PASS_KEY).getAsString(),
            getStringOr(object, NAMESPACE_KEY, null),
            getStringOr(object, DATABASE_KEY, null),
            getStringOr(object, SCOPE_KEY, null)
        );
    }

    @SuppressWarnings("SameParameterValue")
    private @Nullable String getStringOr(JsonObject object, String key, @Nullable String fallback) {
        return object.has(key) ? object.get(key).getAsString() : fallback;
    }
}
