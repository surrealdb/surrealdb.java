package com.surrealdb.gson;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class SurrealGsonUtils {

    private static final @NotNull ImmutableSet<SurrealGsonAdaptor<?>> ADAPTORS = ImmutableSet.of(
        new IdAdaptor(),

        new InstantAdaptor(),

        new GeometryPointAdaptor(),
        new GeometryLineStringAdaptor(),
        new GeometryLinearRingAdaptor(),
        new GeometryPolygonAdaptor(),
        new GeometryMultiPointAdaptor(),
        new GeometryMultiLineStringAdaptor(),
        new GeometryMultiPolygonAdaptor(),
        new GeometryCollectionAdaptor()
    );

    public static @NotNull Gson createSurrealCompatibleGsonInstance(@NotNull Gson gson) {
        GsonBuilder builder = gson.newBuilder();

        // SurrealDB doesn't need HTML escaping
        builder.disableHtmlEscaping();

        // Register all adaptors
        for (SurrealGsonAdaptor<?> adaptor : ADAPTORS) {
            builder.registerTypeAdapter(adaptor.getAdaptorClass(), adaptor);
        }

        return builder.create();
    }
}
