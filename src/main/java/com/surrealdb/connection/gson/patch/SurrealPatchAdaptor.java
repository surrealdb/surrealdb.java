package com.surrealdb.connection.gson.patch;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.surrealdb.connection.gson.SurrealGsonAdaptor;
import com.surrealdb.driver.model.patch.Patch;
import lombok.AllArgsConstructor;

import javax.annotation.Nullable;

@AllArgsConstructor
public abstract class SurrealPatchAdaptor<T extends Patch> implements SurrealGsonAdaptor<T> {

    private final Class<T> adaptorClass;
    private final String op;

    @Override
    public Class<T> getAdaptorClass() {
        return adaptorClass;
    }

    protected JsonObject createObject(T patch, @Nullable String value) {
        JsonObject object = new JsonObject();
        object.add("op", new JsonPrimitive(op));
        object.add("path", new JsonPrimitive(patch.getPath()));
        if (value != null) {
            object.add("value", new JsonPrimitive(value));
        }
        return object;
    }
}
