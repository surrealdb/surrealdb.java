package com.surrealdb.connection;

import lombok.Getter;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class RequestEntry<T> {

    @Nullable
    private final Type resultType;
    @Getter
    private final CompletableFuture<T> callback;

    public RequestEntry(@Nullable Type resultType, CompletableFuture<T> callback) {
        this.resultType = resultType;
        this.callback = callback;
    }

    public Optional<Type> getResultType() {
        return Optional.ofNullable(resultType);
    }
}
