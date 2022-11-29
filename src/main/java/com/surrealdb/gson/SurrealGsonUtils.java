package com.surrealdb.gson;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class SurrealGsonUtils {

    private static final @NotNull ImmutableSet<SurrealGsonAdaptor<?>> ADAPTORS = ImmutableSet.of(
        new InstantAdaptor(),

        new GeometryPointAdaptor(),
        new GeometryLineStringAdaptor(),
        new GeometryPolygonAdaptor(),
        new GeometryMultiPointAdaptor(),
        new GeometryMultiLineStringAdaptor(),
        new GeometryMultiPolygonAdaptor(),
        new GeometryCollectionAdaptor()
    );

    public static @NotNull Gson makeGsonInstanceSurrealCompatible(@NotNull Gson gson) {
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
