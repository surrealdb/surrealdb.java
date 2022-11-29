package com.surrealdb.connection;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * An internal container for a pending request data.
 * Used to store result type for deserialization, as well as a future to callback.
 */
@Value
public
class RequestEntry {

    @NotNull String id;
    @NotNull Instant timestamp;
    @NotNull CompletableFuture<?> callback;
    @NotNull String method;
    @Nullable Type resultType;

    public @NotNull Optional<Type> getResultType() {
        return Optional.ofNullable(resultType);
    }
}
