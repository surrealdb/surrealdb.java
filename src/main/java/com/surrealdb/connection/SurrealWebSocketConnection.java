package com.surrealdb.connection;

import com.google.gson.Gson;
import com.surrealdb.connection.exception.*;
import com.surrealdb.connection.model.RpcRequest;
import com.surrealdb.connection.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Khalid Alharisi
 */
@Slf4j
@ParametersAreNonnullByDefault
public class SurrealWebSocketConnection extends WebSocketClient implements SurrealConnection {

    private final AtomicLong lastRequestId;
    private final Gson gson;
    private final Map<String, RequestEntry<?>> pendingRequests;

    // precomputed private variables
    private final Pattern RECORD_ALREADY_EXITS_PATTERN = Pattern.compile("There was a problem with the database: Database record `(.+):(.+)` already exists");

    /**
     * @param protocol the protocol to use (ws or wss)
     * @param host     The host to connect to
     * @param port     The port to connect to
     * @deprecated Use {@link SurrealConnection#create(SurrealConnectionProtocol, String, int)} instead
     */
    @Deprecated
    public SurrealWebSocketConnection(SurrealConnectionProtocol protocol, String host, int port) {
        this(SurrealConnectionSettings.builder().setUriFromComponents(protocol, host, port).build());
    }

    protected SurrealWebSocketConnection(SurrealConnectionSettings settings) {
        super(settings.getUri());

        this.lastRequestId = new AtomicLong(0);
        this.gson = settings.getGson();
        this.pendingRequests = new HashMap<>();
    }

    @Override
    public void connect(int timeoutSeconds) {
        try {
            log.debug("Connecting to SurrealDB server {}", uri);
            this.connectBlocking(timeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new SurrealConnectionTimeoutException();
        }
        if (!isOpen()) {
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
    public <T> CompletableFuture<T> rpc(@Nullable Type resultType, String method, Object... params) {
        RpcRequest request = new RpcRequest(Long.toString(lastRequestId.incrementAndGet()), method, params);
        CompletableFuture<T> callback = new CompletableFuture<>();

        RequestEntry<T> requestEntry = new RequestEntry<>(resultType, callback);
        pendingRequests.put(request.getId(), requestEntry);

        log.warn("Sending request: {}", request);

        try {
            String json = gson.toJson(request);
            log.debug("Sending RPC request [method: {}, body: {}]", method, json);
            send(json);
        } catch (WebsocketNotConnectedException e) {
            throw new SurrealNotConnectedException();
        }

        return callback;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        log.debug("Connected to SurrealDB server {}", uri);
    }

    @Override
    public void onMessage(String message) {
        log.debug("Received message: {}", message);

        // deserialize the message
        RpcResponse response = gson.fromJson(message, RpcResponse.class);
        String requestId = response.getId();
        RpcResponse.Error error = response.getError();

        // look up the request this response is for
        RequestEntry<?> requestEntry = pendingRequests.get(requestId);
        pendingRequests.remove(requestId);

        if (requestEntry == null) {
            log.warn("Received response for unknown request [id: {}]", requestId);
            return;
        }

        Optional<Type> nullableType = requestEntry.getResultType();
        CompletableFuture<?> callback = requestEntry.getCallback();

        // Wrap everything in a try-catch-finally block so that we can always complete the callback
        try {
            if (!response.isSuccessful()) {
                handleError(requestId, error, callback);
            } else if (nullableType.isPresent()) {
                Type type = nullableType.get();
                callback.complete(gson.fromJson(response.getResult(), type));
            } else {
                callback.complete(null);
            }
        } catch (Exception e) {
            callback.completeExceptionally(e);
        } finally {
            // Make sure the callback is completed no matter what
            if (!callback.isDone()) {
                callback.complete(null);
            }
        }
    }

    private <T> void handleError(String requestId, RpcResponse.Error error, CompletableFuture<T> callback) {
        log.error("Received RPC error: id={} code={} message={}", requestId, error.getCode(), error.getMessage());

        if (error.getMessage().contains("There was a problem with authentication")) {
            callback.completeExceptionally(new SurrealAuthenticationException());
        } else if (error.getMessage().contains("There was a problem with the database: Specify a namespace to use")) {
            callback.completeExceptionally(new SurrealNoDatabaseSelectedException());
        } else {
            Matcher recordAlreadyExitsMatcher = RECORD_ALREADY_EXITS_PATTERN.matcher(error.getMessage());
            if (recordAlreadyExitsMatcher.matches()) {
                callback.completeExceptionally(new SurrealRecordAlreadyExistsException(recordAlreadyExitsMatcher.group(1), recordAlreadyExitsMatcher.group(2)));
            } else {
                callback.completeExceptionally(new SurrealException());
            }
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.debug("Connection closed: code={} reason={} remote={}", code, reason, remote);
        pendingRequests.clear();
    }

    @Override
    public void onError(Exception ex) {
        if (!(ex instanceof ConnectException) && !(ex instanceof NoRouteToHostException)) {
            log.error("Connection error", ex);
        }
    }

}
