package com.surrealdb.client;

import com.surrealdb.client.SurrealClient;
import com.surrealdb.exception.SurrealConnectionTimeoutException;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.surrealdb.client.InternalClientUtils.getResultSynchronously;

/**
 * An interface for all bidirectional clients. Bidirectional clients support live queries.
 */
public non-sealed interface SurrealBiDirectionalClient extends SurrealClient {

    @Override
    default @NotNull CompletableFuture<Void> cleanupAsync() {
        return disconnectAsync();
    }

    /**
     * Connects to the SurrealDB server.
     *
     * @param timeout  the maximum time to wait
     * @param timeUnit the time unit of the timeout argument
     * @return a future that completes when the client has connected
     */
    @NotNull CompletableFuture<Void> connectAsync(long timeout, @NotNull TimeUnit timeUnit);

    /**
     * Connects to the SurrealDB server. This method blocks until the client has connected.
     *
     * @param timeout  the maximum time to wait
     * @param timeUnit the time unit of the timeout argument
     * @throws SurrealConnectionTimeoutException if the connection times out
     */
    default void connect(long timeout, @NotNull TimeUnit timeUnit) {
        getResultSynchronously(connectAsync(timeout, timeUnit));
    }

    /**
     * @return a future that completes when the client has disconnected
     */
    @NotNull CompletableFuture<Void> disconnectAsync();

    /**
     * Disconnects from the SurrealDB server. This method blocks until the client has disconnected.
     */
    default void disconnect() {
        getResultSynchronously(disconnectAsync());
    }

    /**
     * @return true if the client is connected to the SurrealDB server, false otherwise
     */
    boolean isConnected();
}
