package com.surrealdb.gson;

import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.surrealdb.types.SurrealEdgeRecord;
import com.surrealdb.types.SurrealRecord;
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

    public static @NotNull GsonBuilder makeGsonSurrealCompatible(@NotNull GsonBuilder gsonBuilder) {
        // SurrealDB doesn't need HTML escaping
        gsonBuilder.disableHtmlEscaping();

        // Register all adaptors
        for (SurrealGsonAdaptor<?> adaptor : ADAPTORS) {
            gsonBuilder.registerTypeAdapter(adaptor.getAdaptorClass(), adaptor);
        }

        return gsonBuilder;
    }

    public static @NotNull GsonBuilder makeGsonSurrealCompatible(@NotNull Gson gson) {
        return makeGsonSurrealCompatible(gson.newBuilder());
    }

    public static @NotNull Gson createSurrealCompatibleGsonInstance(@NotNull Gson surrealCompatibleUserGson) {
        GsonBuilder gsonBuilder = makeGsonSurrealCompatible(new GsonBuilder());

        gsonBuilder.registerTypeHierarchyAdapter(SurrealRecord.class, new SurrealRecordAdaptor(surrealCompatibleUserGson));
        gsonBuilder.registerTypeHierarchyAdapter(SurrealEdgeRecord.class, new SurrealEdgeRecordAdaptor(surrealCompatibleUserGson));

        return gsonBuilder.create();
    }
}
