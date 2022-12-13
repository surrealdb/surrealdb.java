package com.surrealdb.client.communication;

import lombok.Value;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

/**
 * An internal container for a pending request data.
 * Used to store result type for deserialization, as well as a future to callback.
 *
 * @param <RESULT_TYPE> The type of the deserialized response
 */
@Value
public class RequestEntry<RESULT_TYPE> {

    @NotNull String id;
    @NotNull String endpoint;

    @NotNull Instant timestamp;

    @NotNull CompletableFuture<RESULT_TYPE> callback;
    @NotNull Type resultType;

}
