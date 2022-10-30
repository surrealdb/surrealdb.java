package com.surrealdb.connection.gson;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

/**
 * @param <T> the type of the object to be serialized/deserialized
 * @author Damian Kocher
 */
abstract class SurrealGsonAdaptor<T> implements JsonSerializer<T>, JsonDeserializer<T> {

    private final Class<T> adaptorClass;

    SurrealGsonAdaptor(Class<T> adaptorClass) {
        this.adaptorClass = adaptorClass;
    }

    final Class<T> getAdaptorClass() {
        return adaptorClass;
    }
}
