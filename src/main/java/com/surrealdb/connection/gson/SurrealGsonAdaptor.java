package com.surrealdb.connection.gson;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import com.surrealdb.connection.gson.patch.SurrealAddPatchAdaptor;
import com.surrealdb.connection.gson.patch.SurrealChangePatchAdaptor;
import com.surrealdb.connection.gson.patch.SurrealRemovePatchAdaptor;
import com.surrealdb.connection.gson.patch.SurrealReplacePatchAdaptor;

/**
 * @param <T> the type of the object to be serialized/deserialized
 * @author Damian Kocher
 */
public interface SurrealGsonAdaptor<T> extends JsonSerializer<T>, JsonDeserializer<T> {

    static ImmutableSet<SurrealGsonAdaptor<?>> getAdaptors() {
        return ImmutableSet.of(
            new SurrealInstantAdaptor(),

            new SurrealSigninAdaptor(),

            new SurrealAddPatchAdaptor(),
            new SurrealRemovePatchAdaptor(),
            new SurrealChangePatchAdaptor(),
            new SurrealReplacePatchAdaptor(),

            new SurrealPointAdaptor(),
            new SurrealLineStringAdaptor(),
            new SurrealPolygonAdaptor(),
            new SurrealMultiPointAdaptor(),
            new SurrealMultiLineStringAdaptor()
//            new SurrealMultiPolygonAdaptor(),
//            new SurrealGeometryCollectionAdaptor(),
        );
    }

    Class<T> getAdaptorClass();

}
