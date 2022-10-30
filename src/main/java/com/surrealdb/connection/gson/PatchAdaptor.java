package com.surrealdb.connection.gson;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.surrealdb.driver.model.patch.Patch;

import javax.annotation.Nullable;

abstract class PatchAdaptor<T extends Patch> extends SurrealGsonAdaptor<T> {

    private final String op;

    PatchAdaptor(Class<T> adaptorClass, String op) {
        super(adaptorClass);

        this.op = op;
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
