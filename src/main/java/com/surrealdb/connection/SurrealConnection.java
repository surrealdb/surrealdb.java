package com.surrealdb.connection;

import com.google.gson.Gson;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

/**
 * @author Khalid Alharisi
 */
public interface SurrealConnection {

    /**
     * The preferred way to create a connection to a SurrealDB server. The provided settings
     * will be used to configure the connection.
     * <p>
     * Currently, this method only returns an instance of {@link SurrealWebSocketConnection}. However,
     * future version may return other implementations of {@link SurrealConnection} depending on the
     * provided settings.
     *
     * @param settings the connection settings to use
     * @return a new {@link SurrealConnection} instance
     */
    static @NotNull SurrealConnection create(@NotNull SurrealConnectionSettings settings) {
        return new SurrealWebSocketConnection(settings);
    }

    /**
     * The easiest way to set up a connection to a SurrealDB server. This method will create a
     * connection using the provided protocol, host, and port. All other settings will be left
     * at their default values.
     *
     * @param protocol The protocol to use
     * @param host     The host to connect to
     * @param port     The port to connect to
     * @return a new {@link SurrealConnection} instance
     */
    static @NotNull SurrealConnection create(@NotNull SurrealConnectionProtocol protocol, @NotNull String host, int port) {
        SurrealConnectionSettings settings = SurrealConnectionSettings.builder()
            .setUriFromComponents(protocol, host, port)
            .build();

        return create(settings);
    }

    /**
     * Establishes a connection to the SurrealDB server. This method will block until the connection
     * is established or an error occurs.
     *
     * @param timeoutSeconds The timeout in seconds
     */
    void connect(int timeoutSeconds);

    /**
     * Disconnects from the SurrealDB server. This method will block until the connection is
     * dissolved, or an error occurs.
     */
    void disconnect();

    /**
     * @return true if this {@link SurrealConnection} is connected to a SurrealDB server, false otherwise
     */
    boolean isConnected();

    /**
     * @return the {@link Gson} instance this {@link SurrealConnection} is using to serialize and deserialize
     * data.
     */
    @NotNull Gson getGson();

    /**
     * Sends an RPC request to the SurrealDB server with the given method and parameters. The
     * request will be sent asynchronously, and the returned {@link CompletableFuture} will be
     * completed when the response is received. If the request fails, the future will be completed
     * exceptionally.
     *
     * @param <T>        A generic of the same type as the expected result type
     * @param method     The RPC method to call
     * @param resultType The expected result type
     * @param params     The parameters to pass to the method
     * @return A {@link CompletableFuture} that will be completed with the result of the RPC call,
     * or an exception if the call fails
     */
    <T> @NotNull CompletableFuture<T> rpc(@NotNull ExecutorService executorService, @NotNull String method, @Nullable Type resultType, @NotNull Object... params);

    /**
     * Sends an RPC call to the SurrealDB server without expecting a return value.
     *
     * @param method The RPC method to call
     * @param params The parameters to pass to the method
     * @return A {@link CompletableFuture} that will be completed SurrealDB responds to the RPC call,
     * or an exception if the call fails
     */
    default @NotNull CompletableFuture<Void> rpc(@NotNull ExecutorService executorService, @NotNull String method, @NotNull Object... params) {
        return rpc(executorService, method, null, params);
    }

    default @NotNull <T> CompletableFuture<T> rpc(@NotNull String method, @Nullable Type resultType, @NotNull Object... params) {
        ForkJoinPool executorService = ForkJoinPool.commonPool();
        return rpc(executorService, method, resultType, params);
    }

    default @NotNull CompletableFuture<Void> rpc(@NotNull String method, @NotNull Object... params) {
        ForkJoinPool executorService = ForkJoinPool.commonPool();
        return rpc(executorService, method, params);
    }
}
