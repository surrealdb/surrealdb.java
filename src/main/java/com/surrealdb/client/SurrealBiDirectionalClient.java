package com.surrealdb.client;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.surrealdb.client.InternalClientUtils.getResultSynchronously;

public non-sealed interface SurrealBiDirectionalClient extends SurrealClient {

    @Override
    default @NotNull CompletableFuture<Void> cleanupAsync() {
        return disconnectAsync();
    }

    @NotNull CompletableFuture<Void> connectAsync(long timeout, @NotNull TimeUnit timeUnit);

    default void connect(long timeout, @NotNull TimeUnit timeUnit) {
        getResultSynchronously(connectAsync(timeout, timeUnit));
    }

    @NotNull CompletableFuture<Void> disconnectAsync();

    default void disconnect() {
        getResultSynchronously(disconnectAsync());
    }

    boolean isConnected();
}
