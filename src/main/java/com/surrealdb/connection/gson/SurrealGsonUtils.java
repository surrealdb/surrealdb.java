package com.surrealdb.connection.gson;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SurrealGsonUtils {

    private static final ImmutableSet<SurrealGsonAdaptor<?>> ADAPTORS = ImmutableSet.of(
        new InstantAdaptor(),

        new SignInAdaptor(),

        new PatchAddAdaptor(),
        new PatchRemoveAdaptor(),
        new PatchChangeAdaptor(),
        new PatchReplaceAdaptor(),

        new GeometryPointAdaptor(),
        new GeometryLineAdaptor(),
        new GeometryPolygonAdaptor(),
        new GeometryMultiPointAdaptor(),
        new GeometryMultiLineAdaptor(),
        new GeometryMultiPolygonAdaptor(),
        new GeometryCollectionAdaptor()
    );

    public static Gson makeGsonInstanceSurrealCompatible(Gson gson) {
        GsonBuilder gsonBuilder = gson.newBuilder();

        // SurrealDB doesn't need HTML escaping
        gsonBuilder.disableHtmlEscaping();

        // Register all adaptors
        for (SurrealGsonAdaptor<?> adaptor : ADAPTORS) {
            gsonBuilder.registerTypeAdapter(adaptor.getAdaptorClass(), adaptor);
        }

        return gsonBuilder.create();
    }
}
