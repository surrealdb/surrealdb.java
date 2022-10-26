package com.surrealdb.connection;

import lombok.Value;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * An internal container for a pending request data.
 * Used to store result type for deserialization, as well as a future to callback.
 *
 * @param <T> The type of the result
 *
 * @author Damian Kocher
 */
@Value
class RequestEntry<T> {

    @Nullable
    Type resultType;
    CompletableFuture<T> callback;

    public Optional<Type> getResultType() {
        return Optional.ofNullable(resultType);
    }
}
