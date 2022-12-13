package com.surrealdb.client.bidirectional;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.surrealdb.auth.SurrealAuthCredentials;
import com.surrealdb.client.SurrealBiDirectionalClient;
import com.surrealdb.client.settings.SurrealClientSettings;
import com.surrealdb.client.settings.SurrealConnectionProtocol;
import com.surrealdb.exception.SurrealException;
import com.surrealdb.exception.SurrealExceptionUtils;
import com.surrealdb.query.QueryResult;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static com.surrealdb.gson.SurrealGsonUtils.createSurrealCompatibleGsonInstance;

/**
 * A WebSocket based SurrealDB client.
 */
public class SurrealWebSocketClient implements SurrealBiDirectionalClient {

    @NotNull SurrealClientSettings settings;
    @NotNull Gson gson;

    @NonFinal
    @NotNull InternalWebsocketClient client;

    private SurrealWebSocketClient(@NotNull SurrealClientSettings settings) {
        this.settings = settings;

        this.gson = createSurrealCompatibleGsonInstance(settings.getGson());
        this.client = new InternalWebsocketClient(settings, gson, this::onClose);
    }

    /**
     * Creates a new WebSocket client.
     *
     * @param settings The settings to use to configure the client
     * @return A new {@link SurrealWebSocketClient} instance
     */
    public static @NotNull SurrealWebSocketClient create(@NotNull SurrealClientSettings settings) {
        return new SurrealWebSocketClient(settings);
    }

    /**
     * @param protocol The protocol to use (must be either {@code WEBSOCKET} or {@code WEB_SOCKET_SSL})
     * @param host     The host to connect to
     * @param port     The port to connect to
     * @return A new {@link SurrealWebSocketClient} instance
     */
    public static @NotNull SurrealWebSocketClient create(@NotNull SurrealConnectionProtocol protocol, @NotNull String host, int port) {
        SurrealClientSettings settings = SurrealClientSettings.builder()
            .setUriFromComponents(protocol, host, port)
            .build();
        return create(settings);
    }

    private void onClose(boolean closedByRemote) {
        this.client = new InternalWebsocketClient(settings, gson, this::onClose);
    }

    private @NotNull <T> CompletableFuture<T> rpc(@NotNull String method, @NotNull Type resultType, @NotNull Object... params) {
        return client.rpc(method, resultType, params);
    }

    private @NotNull CompletableFuture<Void> rpc(@NotNull String method, @NotNull Object... params) {
        return client.rpc(method, void.class, params);
    }

    @Override
    public @NotNull CompletableFuture<Void> connectAsync(long timeout, @NotNull TimeUnit timeUnit) {
        return client.connectAsync(timeout, timeUnit);
    }

    @Override
    public @NotNull CompletableFuture<Void> signInAsync(@NotNull SurrealAuthCredentials credentials) {
        return rpc("signin", credentials);
    }

    @Override
    public @NotNull CompletableFuture<Void> signOutAsync() {
        return rpc("invalidate");
    }

    @Override
    public @NotNull CompletableFuture<Void> disconnectAsync() {
        return client.disconnectAsync();
    }

    @Override
    public boolean isConnected() {
        return client.isOpen();
    }

    @Override
    public @NotNull CompletableFuture<Void> setNamespaceAndDatabaseAsync(@NotNull String namespace, @NotNull String database) {
        return rpc("use", namespace, database);
    }

    @Override
    public @NotNull CompletableFuture<Void> pingAsync() {
        return rpc("ping");
    }

    @Override
    public @NotNull CompletableFuture<Void> setConnectionWideParameterAsync(@NotNull String key, @NotNull Object value) {
        return rpc("let", key, value);
    }

    @Override
    public @NotNull CompletableFuture<Void> unsetConnectionWideParameterAsync(@NotNull String key) {
        return rpc("unset", key);
    }

    @Override
    public @NotNull CompletableFuture<String> databaseVersionAsync() {
        return rpc("version", String.class);
    }

    @Override
    public @NotNull <T> CompletableFuture<List<QueryResult<T>>> sqlAsync(@NotNull String query, @NotNull Class<T> queryResult, @NotNull Map<String, Object> args) {
        // QueryResult<T>
        TypeToken<?> queryType = TypeToken.getParameterized(QueryResult.class, queryResult);
        // List<QueryResult<T>>
        Type resultType = TypeToken.getParameterized(List.class, queryType.getType()).getType();
        // Execute the query
        CompletableFuture<List<QueryResult<T>>> queryFuture = args.size() > 0 ?
            rpc("query", resultType, query, args) : // query with args
            rpc("query", resultType, query);        // query without args
        // Check for errors and return the result
        ExecutorService executorService = getAsyncOperationExecutorService();
        return queryFuture.thenComposeAsync(this::checkResultsForErrors, executorService);
    }

    private <T> @NotNull CompletableFuture<List<QueryResult<T>>> checkResultsForErrors(@NotNull List<QueryResult<T>> queryResults) {
        for (QueryResult<T> queryResult : queryResults) {
            if (queryResult.getStatus().equals("ERR")) {
                String errorMessage = queryResult.getDetail().orElse("");
                SurrealException exception = SurrealExceptionUtils.createExceptionFromMessage(errorMessage);
                return CompletableFuture.failedFuture(exception);
            }
        }

        return CompletableFuture.completedFuture(queryResults);
    }

    @Override
    public @NotNull Gson getGson() {
        return gson;
    }

    @Override
    public @NotNull ExecutorService getAsyncOperationExecutorService() {
        return settings.getAsyncOperationExecutorService();
    }
}
