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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static com.surrealdb.connection.gson.SurrealGsonUtils.makeGsonInstanceSurrealCompatible;

/**
 * @author Khalid Alharisi
 */
@Slf4j
public class SurrealWebSocketConnection extends WebSocketClient implements SurrealConnection {

    @NotNull Gson gson;

    @NotNull AtomicLong lastRequestId;
    @NonFinal
    @NotNull Map<String, RequestEntry> pendingRequests;

    boolean logOutgoingMessages;
    boolean logIncomingMessages;
    boolean logAuthenticationCredentials;

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
        super(settings.getUri());

        this.gson = makeGsonInstanceSurrealCompatible(settings.getGson());

        this.lastRequestId = new AtomicLong(0);
        this.pendingRequests = new ConcurrentHashMap<>();

        this.logOutgoingMessages = settings.isLogOutgoingMessages();
        this.logIncomingMessages = settings.isLogIncomingMessages();
        this.logAuthenticationCredentials = settings.isLogAuthenticationCredentials();

        if (settings.isAutoConnect()) {
            connect(settings.getDefaultConnectTimeoutSeconds());
        }
    }

    @Override
    public void connect(int timeoutSeconds) {
        if (isConnected()) {
            return;
        }

        try {
            log.debug("Connecting to SurrealDB server {}", uri);
            this.connectBlocking(timeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new SurrealConnectionTimeoutException();
        }

        if (!isConnected()) {
            throw new SurrealConnectionTimeoutException();
        }
    }

    @Override
    public void disconnect() {
        try {
            this.closeBlocking();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isConnected() {
        return this.isOpen();
    }

    @Override
    public @NotNull Gson getGson() {
        return gson;
    }

    @Override
    public <T> @NotNull CompletableFuture<T> rpc(@NotNull ExecutorService executorService, @NotNull String method, @Nullable Type resultType, Object @NotNull ... params) {
        return CompletableFuture.supplyAsync(() -> {
            String requestId = Long.toString(lastRequestId.incrementAndGet());
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

    private void logRpcRequest(@NotNull String method, String requestId, String json) {
        if (!logOutgoingMessages) {
            return;
        }

        String body = json;
        // Only log sign in credentials if explicitly enabled
        if (method.equals("signin") && !logAuthenticationCredentials) {
            body = "REDACTED";
        }
        log.debug("Outgoing RPC [id: {}, method: {}, request: {}]", requestId, method, body);
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        log.debug("Connected to SurrealDB server {}", uri);
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

        Optional<Type> resultType = requestEntry.getResultType();
        CompletableFuture<?> callback = requestEntry.getCallback();

        try {
            Optional<RpcResponse.Error> error = response.getError();

            if (error.isPresent()) {
                RpcResponse.Error error1 = error.get();
                String errorMessage = error1.getMessage();
                if (logIncomingMessages) {
                    log.error("Received RPC error [id: {}, code: {}, message: {}]", requestId, error1.getCode(), errorMessage);
                }
                SurrealException exception = SurrealExceptionUtils.createExceptionFromMessage(errorMessage);
                callback.completeExceptionally(exception);
                return;
            }

            logSuccessfulRpcResponse(requestEntry, message);

            if (!resultType.isPresent()) {
                callback.complete(null);
                return;
            }

            callback.complete(gson.fromJson(response.getResult(), resultType.get()));
        } catch (Exception e) {
            callback.completeExceptionally(e);
        }
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
    public void onClose(int code, String reason, boolean remote) {
        log.debug("Connection closed [code: {}, reason: {}, remote: {}]", code, reason, remote);

        Map<String, RequestEntry> oldPendingRequests = pendingRequests;
        pendingRequests = new ConcurrentHashMap<>();

        for (RequestEntry value : oldPendingRequests.values()) {
            value.getCallback().completeExceptionally(new SurrealNotConnectedException());
        }
    }

    @Override
    public void onError(Exception ex) {
        if (!(ex instanceof ConnectException) && !(ex instanceof NoRouteToHostException)) {
            log.error("Connection error", ex);
        }
    }
}
