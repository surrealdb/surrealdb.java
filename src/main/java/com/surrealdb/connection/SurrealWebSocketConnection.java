package com.surrealdb.connection;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.surrealdb.connection.exception.SurrealConnectionTimeoutException;
import com.surrealdb.connection.exception.SurrealException;
import com.surrealdb.connection.exception.SurrealExceptionUtils;
import com.surrealdb.connection.exception.SurrealNotConnectedException;
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
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import static com.surrealdb.connection.gson.SurrealGsonUtils.makeGsonInstanceSurrealCompatible;

/**
 * @author Khalid Alharisi
 */
@Slf4j
public class SurrealWebSocketConnection implements SurrealConnection {

    @NotNull SurrealConnectionSettings settings;
    @NotNull Gson gson;

    @NonFinal
    @NotNull InternalWebsocketClient client;

    /**
     * @param host   The host to connect to
     * @param port   The port to connect to
     * @param useTls Whether to use TLS or not
     * @deprecated use {@link SurrealWebSocketConnection#create(SurrealConnectionSettings)},
     * {@link SurrealConnection#create(SurrealConnectionProtocol, String, int)} or
     * {@link SurrealConnection#create(SurrealConnectionProtocol, String, int)} instead
     */
    @Deprecated
    public SurrealWebSocketConnection(@NotNull String host, int port, boolean useTls) {
        this(SurrealConnectionSettings.builder()
            .setUriFromComponents(useTls ? SurrealConnectionProtocol.WEB_SOCKET_SSL : SurrealConnectionProtocol.WEB_SOCKET, host, port)
            .build());
    }

    /**
     * Creates a new connection to a SurrealDB server using the given settings. If auto connect is enabled, the connection
     * will be established immediately, meaning that the constructor will block until the connection is established.
     *
     * @param settings The settings to use
     */
    public SurrealWebSocketConnection(@NotNull SurrealConnectionSettings settings) {
        this.settings = settings;
        this.gson = makeGsonInstanceSurrealCompatible(settings.getGson());

        this.client = new InternalWebsocketClient(settings, gson, this::onClose);

        if (settings.isAutoConnect()) {
            connect(settings.getDefaultConnectTimeoutSeconds());
        }
    }

    @Override
    public void connectAsync(int timeoutSeconds) {
        client.connectAsync(timeoutSeconds);
    }

    @Override
    public void connect(int timeoutSeconds) {
        client.connect(timeoutSeconds);
    }

    @Override
    public void disconnectAsync() {
        client.disconnectAsync();
    }

    @Override
    public void disconnect() {
        client.disconnect();
    }

    @Override
    public boolean isConnected() {
        return client.isConnected();
    }

    @Override
    public @NotNull Gson getGson() {
        return gson;
    }

    @Override
    public @NotNull <T> CompletableFuture<T> rpc(@NotNull ExecutorService executorService, @NotNull String method, @Nullable Type resultType, @NotNull Object... params) {
        return client.rpc(executorService, method, resultType, params);
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
            connect(settings.getDefaultConnectTimeoutSeconds());
        } catch (SurrealConnectionTimeoutException e) {
            log.warn("Failed to reconnect to SurrealDB server (attempt {})", currentAttempt);
            reconnect(currentAttempt);
        }
    }

    @Slf4j
    private static class InternalWebsocketClient extends WebSocketClient implements SurrealConnection {

        @NotNull Gson gson;
        @NotNull AtomicLong lastRequestId;
        @NotNull Map<String, RequestEntry> pendingRequests;

        @NotNull Consumer<Boolean> onClose;

        boolean logOutgoingMessages;
        boolean logIncomingMessages;
        boolean logAuthenticationCredentials;

        InternalWebsocketClient(@NotNull SurrealConnectionSettings settings, @NotNull Gson gson, @NotNull Consumer<Boolean> onClose) {
            super(settings.getUri());

            this.gson = gson;
            this.onClose = onClose;

            this.lastRequestId = new AtomicLong();
            this.pendingRequests = new ConcurrentHashMap<>();

            this.logOutgoingMessages = settings.isLogOutgoingMessages();
            this.logIncomingMessages = settings.isLogIncomingMessages();
            this.logAuthenticationCredentials = settings.isLogAuthenticationCredentials();
        }

        @Override
        public <T> @NotNull CompletableFuture<T> rpc(@NotNull ExecutorService executorService, @NotNull String method, @Nullable Type resultType, Object @NotNull ... params) {
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

        @Override
        public void connectAsync(int timeoutSeconds) {
            CompletableFuture.runAsync(() -> connect(timeoutSeconds));
        }

        @Override
        public void connect(int timeoutSeconds) {
            if (isConnected()) {
                log.debug("Already connected, ignoring connect request");
                return;
            }

            try {
                log.debug("Connecting to SurrealDB server {}", uri);
                connectBlocking(timeoutSeconds, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new SurrealConnectionTimeoutException();
            }

            if (!isConnected()) {
                throw new SurrealConnectionTimeoutException();
            }
        }

        @Override
        public void disconnectAsync() {
            CompletableFuture.runAsync(this::disconnect);
        }

        @Override
        public void disconnect() {
            log.debug("Disconnecting from SurrealDB server {}", uri);
            try {
                closeBlocking();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public boolean isConnected() {
            return isOpen();
        }

        // This is only needed to satisfy the interface
        @Override
        public @NotNull Gson getGson() {
            return gson;
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
