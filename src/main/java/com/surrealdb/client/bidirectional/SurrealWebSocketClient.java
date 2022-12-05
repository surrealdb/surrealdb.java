package com.surrealdb.client.bidirectional;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.surrealdb.auth.SurrealAuthCredentials;
import com.surrealdb.client.SurrealBiDirectionalClient;
import com.surrealdb.client.bidirectional.rpc.RpcRequest;
import com.surrealdb.client.bidirectional.rpc.RpcResponse;
import com.surrealdb.client.communication.CommunicationManager;
import com.surrealdb.client.communication.RequestEntry;
import com.surrealdb.client.listener.ListenerManager;
import com.surrealdb.client.listener.SurrealGenericLogListener;
import com.surrealdb.client.settings.SurrealClientSettings;
import com.surrealdb.client.settings.SurrealConnectionProtocol;
import com.surrealdb.exception.SurrealConnectionTimeoutException;
import com.surrealdb.exception.SurrealException;
import com.surrealdb.exception.SurrealExceptionUtils;
import com.surrealdb.exception.SurrealNotConnectedException;
import com.surrealdb.query.QueryResult;
import lombok.experimental.NonFinal;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.surrealdb.gson.SurrealGsonUtils.makeGsonInstanceSurrealCompatible;

/**
 * A WebSocket based SurrealDB client.
 */
public class SurrealWebSocketClient implements SurrealBiDirectionalClient {

    @NotNull SurrealClientSettings settings;
    @NotNull Gson gson;
    @NotNull ListenerManager<RpcRequest> listenerManager;

    @NonFinal
    @NotNull InternalWebsocketClient client;

    private SurrealWebSocketClient(@NotNull SurrealClientSettings settings) {
        this.settings = settings;

        this.gson = makeGsonInstanceSurrealCompatible(settings.getGson());
        this.listenerManager = new ListenerManager<>();
        this.client = new InternalWebsocketClient(settings, gson, listenerManager, this::onClose);
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
        this.client = new InternalWebsocketClient(settings, gson, listenerManager, this::onClose);
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
        CompletableFuture<List<QueryResult<T>>> queryFuture = rpc("query", resultType, query, args);
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

    private static class InternalWebsocketClient extends WebSocketClient {

        @NotNull Gson gson;
        @NotNull ListenerManager<RpcRequest> listenerManager;
        @NotNull CommunicationManager<RpcRequest> communicationManager;
        @NotNull AtomicLong lastRequestId;

        @NotNull Consumer<Boolean> onClose;

        @NotNull ExecutorService executorService;

        InternalWebsocketClient(@NotNull SurrealClientSettings settings, @NotNull Gson gson, @NotNull ListenerManager<RpcRequest> listenerManager, @NotNull Consumer<Boolean> onClose) {
            super(settings.getUri());

            this.gson = gson;
            this.listenerManager = listenerManager;
            this.communicationManager = new CommunicationManager<>(gson, listenerManager);
            this.onClose = onClose;

            this.lastRequestId = new AtomicLong();

            this.executorService = settings.getAsyncOperationExecutorService();
        }

        public <U> @NotNull CompletableFuture<U> rpc(@NotNull String method, @NotNull Type resultType, Object @NotNull ... arguments) {
            return CompletableFuture.supplyAsync(() -> {
                String id = Long.toString(lastRequestId.getAndIncrement());
                RpcRequest rpcRequest = new RpcRequest(id, method, arguments);

                try {
                    RequestEntry<RpcRequest, U> requestEntry = communicationManager.createRequest(id, method, resultType, rpcRequest);
                    String payloadString = gson.toJson(rpcRequest);

                    send(payloadString);

                    return requestEntry.getCallback();
                } catch (Exception exception) {
                    communicationManager.cancelRequest(id, exception);

                    if (exception instanceof WebsocketNotConnectedException) {
                        throw new SurrealNotConnectedException();
                    }

                    throw SurrealExceptionUtils.wrapException("Failed to send RPC request", exception);
                }
            }, executorService).thenCompose(Function.identity());
        }

        @Override
        public void onMessage(@NotNull String rawMessage) {
            JsonElement message = gson.fromJson(rawMessage, JsonElement.class);
            RpcResponse response = gson.fromJson(message, RpcResponse.class);

            response.getError().ifPresentOrElse(
                error -> communicationManager.completeRequest(response.getId(), error),
                () -> communicationManager.completeRequest(response.getId(), response.getResult())
            );
        }

        @NotNull CompletableFuture<Void> connectAsync(long timeout, @NotNull TimeUnit timeUnit) {
            return CompletableFuture.runAsync(() -> {
                if (isOpen()) {
                    listenerManager.onLog(SurrealGenericLogListener.Type.ATTEMPTING_TO_CONNECT_WHILE_ALREADY_CONNECTED, "Already connected, ignoring connect request");
                    return;
                }

                try {
                    listenerManager.onLog(SurrealGenericLogListener.Type.CONNECTING_TO_SERVER, () -> String.format("Connecting to %s", getURI()));
                    connectBlocking(timeout, timeUnit);
                } catch (InterruptedException ignored) {
                }

                if (!isOpen()) {
                    throw new SurrealConnectionTimeoutException();
                }
            }, executorService);
        }

        public @NotNull CompletableFuture<Void> disconnectAsync() {
            return CompletableFuture.runAsync(() -> {
                try {
                    listenerManager.onLog(SurrealGenericLogListener.Type.DISCONNECTING_FROM_SERVER, () -> String.format("Disconnecting from %s", getURI()));
                    closeBlocking();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }, executorService);
        }

        @Override
        public void onOpen(ServerHandshake handshakeData) {
            listenerManager.onConnectionStateChange(true, false);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            listenerManager.onConnectionStateChange(false, remote);
            communicationManager.cancelAllRequests(new SurrealNotConnectedException());
            onClose.accept(remote);
        }

        @Override
        public void onError(Exception exception) {
            listenerManager.onException(exception);
        }
    }
}