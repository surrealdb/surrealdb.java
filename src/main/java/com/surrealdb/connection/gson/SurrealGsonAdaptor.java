package com.surrealdb.connection.gson;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

/**
 * @param <T> the type of the object to be serialized/deserialized
 * @author Damian Kocher
 */
abstract class SurrealGsonAdaptor<T> implements JsonSerializer<T>, JsonDeserializer<T> {

    @NotNull Class<T> adaptorClass;

    SurrealGsonAdaptor(@NotNull Class<T> adaptorClass) {
        this.adaptorClass = adaptorClass;
    }

    final @NotNull Type getAdaptorClass() {
        return adaptorClass;
    }
}
