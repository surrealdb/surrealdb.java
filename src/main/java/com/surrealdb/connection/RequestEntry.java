package com.surrealdb.connection;

import lombok.Value;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Value
class RequestEntry<T> {

    @Nullable
    Type resultType;
    CompletableFuture<T> callback;

    public Optional<Type> getResultType() {
        return Optional.ofNullable(resultType);
    }
}
