package com.surrealdb.connection;

import lombok.Value;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * An internal container for a pending request data.
 * Used to store result type for deserialization, as well as a future to callback.
 *
 * @author Damian Kocher
 */
@Value
class RequestEntry {

    String id;
    Instant timestamp;
    CompletableFuture<?> callback;
    String method;
    @Nullable
    Type resultType;

    public Optional<Type> getResultType() {
        return Optional.ofNullable(resultType);
    }
}
