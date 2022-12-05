package com.surrealdb.client.communication;

import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

/**
 * An internal container for a pending request data.
 * Used to store result type for deserialization, as well as a future to callback.
 * @param <T> The type of the request payload
 * @param <U> The type of the deserialized response
 */
@Value
public class RequestEntry<T, U> {

    @NotNull String id;
    @NotNull String method;

    @NotNull Instant timestamp;

    @NotNull T payload;

    @NotNull CompletableFuture<U> callback;
    @NotNull Type resultType;

}
