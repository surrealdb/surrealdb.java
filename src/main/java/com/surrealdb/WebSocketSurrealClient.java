package com.surrealdb;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.surrealdb.exception.SurrealConnectionTimeoutException;
import com.surrealdb.exception.SurrealException;
import com.surrealdb.exception.SurrealExceptionUtils;
import com.surrealdb.exception.SurrealNotConnectedException;
import com.surrealdb.auth.SurrealAuthCredentials;
import com.surrealdb.sql.QueryResult;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static com.surrealdb.gson.SurrealGsonUtils.makeGsonInstanceSurrealCompatible;

@Slf4j
public class WebSocketSurrealClient implements BiDirectionalSurrealClient {

    @NotNull SurrealClientSettings settings;
    @NotNull Gson gson;

    @NonFinal
    @NotNull InternalWebsocketClient client;

    private WebSocketSurrealClient(@NotNull SurrealClientSettings settings) {
        this.settings = settings;

        this.gson = makeGsonInstanceSurrealCompatible(settings.getGson());
        this.client = new InternalWebsocketClient(settings, gson, this::onClose);
    }

    public static @NotNull WebSocketSurrealClient create(@NotNull SurrealClientSettings settings) {
        return new WebSocketSurrealClient(settings);
    }

    public static @NotNull WebSocketSurrealClient create(@NotNull SurrealConnectionProtocol protocol, @NotNull String host, int port) {
        SurrealClientSettings settings = SurrealClientSettings.builder()
            .setUriFromComponents(protocol, host, port)
            .build();
        return create(settings);
    }

    private void onClose(boolean closedByRemote) {
        this.client = new InternalWebsocketClient(settings, gson, this::onClose);

        if (closedByRemote && settings.isReconnectOnUnexpectedDisconnect()) {
            reconnect(0);
        }
    }

    private void reconnect(int attemptCount) {
        if (attemptCount >= settings.getMaxReconnectAttempts()) {
            log.error("Failed to reconnect to SurrealDB server after {} attempts", attemptCount);
            return;
        }

        int currentAttempt = attemptCount + 1;
        log.info("Reconnecting to SurrealDB server (attempt {})", currentAttempt);

        try {
            connect(settings.getDefaultConnectTimeoutSeconds(), TimeUnit.SECONDS);
        } catch (SurrealConnectionTimeoutException e) {
            log.warn("Failed to reconnect to SurrealDB server (attempt {})", currentAttempt);
            reconnect(currentAttempt);
        }
    }

    private @NotNull <T> CompletableFuture<T> rpc(@NotNull String method, @Nullable Type resultType, @NotNull Object... params) {
        return client.rpc(method, resultType, params);
    }

    @Override
    public @NotNull CompletableFuture<Void> connectAsync(int timeout, @NotNull TimeUnit timeUnit) {
        return client.connectAsync(timeout, timeUnit);
    }

    @Override
    public @NotNull CompletableFuture<Void> signInAsync(@NotNull SurrealAuthCredentials credentials) {
        return rpc("signin", null, credentials);
    }

    @Override
    public @NotNull CompletableFuture<Void> signOutAsync() {
        return rpc("invalidate", null);
    }

    @Override
    public @NotNull CompletableFuture<Void> disconnectAsync() {
        return client.disconnectAsync();
    }

    @Override
    public @NotNull CompletableFuture<Void> useAsync(@NotNull String namespace, @NotNull String database) {
        return rpc("use", null, namespace, database);
    }

    @Override
    public @NotNull CompletableFuture<Void> pingAsync() {
        return rpc("ping", null);
    }

    @Override
    public @NotNull CompletableFuture<Void> setConnectionWideParameterAsync(@NotNull String key, @NotNull Object value) {
        return rpc("let", null, key, value);
    }

    @Override
    public @NotNull CompletableFuture<Void> unsetConnectionWideParameterAsync(@NotNull String key) {
        return rpc("unset", null, key);
    }

    @Override
    public @NotNull CompletableFuture<String> databaseVersionAsync() {
        return rpc("version", String.class);
    }

    @Override
    public @NotNull CompletableFuture<Map<String, String>> infoAsync() {
        return null;
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

    @Slf4j
    private static class InternalWebsocketClient extends WebSocketClient {

        @NotNull Gson gson;
        @NotNull AtomicLong lastRequestId;
        @NotNull Map<String, RequestEntry> pendingRequests;

        @NotNull Consumer<Boolean> onClose;

        boolean logOutgoingMessages;
        boolean logIncomingMessages;
        boolean logAuthenticationCredentials;

        @NotNull ExecutorService executorService;

        InternalWebsocketClient(@NotNull SurrealClientSettings settings, @NotNull Gson gson, @NotNull Consumer<Boolean> onClose) {
            super(settings.getUri());

            this.gson = gson;
            this.onClose = onClose;

            this.lastRequestId = new AtomicLong();
            this.pendingRequests = new ConcurrentHashMap<>();

            this.logOutgoingMessages = settings.isLogOutgoingMessages();
            this.logIncomingMessages = settings.isLogIncomingMessages();
            this.logAuthenticationCredentials = settings.isLogAuthenticationCredentials();

            this.executorService = settings.getAsyncOperationExecutorService();
        }

        public <T> @NotNull CompletableFuture<T> rpc(@NotNull String method, @Nullable Type resultType, Object @NotNull ... params) {
            return CompletableFuture.supplyAsync(() -> {
                String requestId = Long.toString(lastRequestId.getAndIncrement());
                Instant timestamp = Instant.now();
                CompletableFuture<T> callback = new CompletableFuture<>();

                RequestEntry requestEntry = new RequestEntry(requestId, timestamp, callback, method, resultType);
                pendingRequests.put(requestId, requestEntry);

                try {
                    // Create a model for serialization
                    RpcRequest request = new RpcRequest(requestId, method, params);
                    // Serialize the model
                    String json = gson.toJson(request);
                    // Log the request
                    logRpcRequest(method, requestId, json);
                    // Send the request
                    send(json);
                } catch (WebsocketNotConnectedException ignored) {
                    callback.completeExceptionally(new SurrealNotConnectedException());
                } catch (Exception e) {
                    callback.completeExceptionally(new SurrealException("Failed to send RPC request", e));
                }

                // If there was an error sending the request, remove the request entry from the map
                // since we will never receive a response for it
                if (callback.isCompletedExceptionally()) {
                    pendingRequests.remove(requestId);
                }

                return callback;
            }, executorService).thenComposeAsync(future -> future, executorService);
        }

        private void logRpcRequest(@NotNull String method, @NotNull String requestId, @NotNull String json) {
            if (!logOutgoingMessages) {
                return;
            }

            String body = method.equals("signin") && !logAuthenticationCredentials ? "REDACTED" : json;
            log.debug("Outgoing RPC [id: {}, method: {}, request: {}]", requestId, method, body);
        }

        @Override
        public void onMessage(String message) {
            // Deserialize the message
            RpcResponse response;
            try {
                response = gson.fromJson(message, RpcResponse.class);
            } catch (JsonSyntaxException e) {
                log.error("Failed to deserialize message from SurrealDB server", e);
                return;
            }
            // Pull out the request id
            String requestId = response.getId();
            // Look up the request this response is for
            RequestEntry requestEntry = pendingRequests.get(requestId);
            // If there is no request entry for this response, ignore it
            if (requestEntry == null) {
                log.warn("Received response for unknown request [id: {}]", requestId);
                return;
            }
            // Remove the request entry from the map
            pendingRequests.remove(requestId);

            CompletableFuture<?> callback = requestEntry.getCallback();
            try {
                response.getError().ifPresentOrElse(
                    error -> handleRpcError(requestEntry, error),
                    () -> handleRpcSuccess(requestEntry, response, message)
                );
            } catch (Exception e) {
                log.error("Failed to handle RPC response", e);
                callback.completeExceptionally(e);
            }

            if (!callback.isDone()) {
                log.error("RPC callback was not completed by the handler");
                callback.completeExceptionally(new SurrealException("RPC callback was not completed by the handler"));
            }
        }

        private void handleRpcError(@NotNull RequestEntry requestEntry, @NotNull RpcResponse.Error error) {
            String errorMessage = error.getMessage();
            CompletableFuture<?> callback = requestEntry.getCallback();

            if (logIncomingMessages) {
                int errorCode = error.getCode();
                log.error("Received RPC error [id: {}, code: {}, message: {}]", requestEntry.getId(), errorCode, errorMessage);
            }

            SurrealException exception = SurrealExceptionUtils.createExceptionFromMessage(errorMessage);
            callback.completeExceptionally(exception);
        }

        private void handleRpcSuccess(RequestEntry requestEntry, @NotNull RpcResponse response, String message) {
            logSuccessfulRpcResponse(requestEntry, message);

            requestEntry.getResultType().ifPresentOrElse(
                resultType -> requestEntry.getCallback().complete(gson.fromJson(response.getResult(), resultType)),
                () -> requestEntry.getCallback().complete(null)
            );
        }

        private void logSuccessfulRpcResponse(@NotNull RequestEntry requestEntry, String message) {
            if (!logIncomingMessages) {
                return;
            }

            String id = requestEntry.getId();
            String method = requestEntry.getMethod();

            log.debug("Incoming RPC [id: {}, method: {}, response: {}]", id, method, message);
        }

        public CompletableFuture<Void> connectAsync(int timeout, @NotNull TimeUnit timeUnit) {
            return CompletableFuture.runAsync(() -> {
                if (isConnected()) {
                    log.debug("Already connected, ignoring connect request");
                    return;
                }

                try {
                    log.debug("Connecting to SurrealDB server {}", uri);
                    connectBlocking(timeout, timeUnit);
                } catch (InterruptedException e) {
                    throw new SurrealConnectionTimeoutException();
                }

                if (!isConnected()) {
                    throw new SurrealConnectionTimeoutException();
                }
            }, executorService);
        }

        public @NotNull CompletableFuture<Void> disconnectAsync() {
            return CompletableFuture.runAsync(() -> {
                log.debug("Disconnecting from SurrealDB server {}", uri);
                try {
                    closeBlocking();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }, executorService);
        }

        public boolean isConnected() {
            return isOpen();
        }

        @Override
        public void onOpen(ServerHandshake handshakeData) {
            log.debug("Connected to SurrealDB server {}", uri);
        }

        @Override
        public void onClose(int code, String reason, boolean remote) {
            log.debug("Connection closed [code: {}, reason: {}, remote: {}]", code, reason, remote);
            onClose.accept(remote);

            for (RequestEntry value : pendingRequests.values()) {
                value.getCallback().completeExceptionally(new SurrealNotConnectedException());
            }
        }

        @Override
        public void onError(Exception exception) {
            if (!(exception instanceof ConnectException) && !(exception instanceof NoRouteToHostException)) {
                log.error("Connection error", exception);
            }
        }
    }
}
