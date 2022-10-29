package com.surrealdb.connection.gson;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;

/**
 * @param <T> the type of the object to be serialized/deserialized
 * @author Damian Kocher
 */
public interface SurrealGsonAdaptor<T> extends JsonSerializer<T>, JsonDeserializer<T> {

    static ImmutableSet<SurrealGsonAdaptor<?>> getAdaptors() {
        return ImmutableSet.of(
            new SurrealInstantAdaptor(),

            new SurrealAddPatchAdaptor(),
            new SurrealChangePatchAdaptor(),
            new SurrealRemovePatchAdaptor(),
            new SurrealReplacePatchAdaptor()
        );
    }

    Class<T> getAdaptorClass();

}
