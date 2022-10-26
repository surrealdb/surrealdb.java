package com.surrealdb.connection;

import com.google.gson.Gson;
import com.surrealdb.connection.exception.*;
import com.surrealdb.connection.model.RpcRequest;
import com.surrealdb.connection.model.RpcResponse;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;

import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Khalid Alharisi
 */
@Slf4j
public class SurrealWebSocketConnection extends WebSocketClient implements SurrealConnection {

    private final AtomicLong lastRequestId;
    private final Gson gson;
    private final Map<String, CompletableFuture<?>> callbacks;
    private final Map<String, Type> resultTypes;

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
        this.callbacks = new HashMap<>();
        this.resultTypes = new HashMap<>();
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
    public <T> CompletableFuture<T> rpc(Type resultType, String method, Object... params) {
        RpcRequest request = new RpcRequest(lastRequestId.incrementAndGet() + "", method, params);
        CompletableFuture<T> callback = new CompletableFuture<>();

        callbacks.put(request.getId(), callback);
        if (resultType != null) {
            resultTypes.put(request.getId(), resultType);
        }

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
        final RpcResponse response = gson.fromJson(message, RpcResponse.class);
        final String id = response.getId();
        final RpcResponse.Error error = response.getError();
        final CompletableFuture<Object> callback = (CompletableFuture<Object>) callbacks.get(id);

        try {
            if (error == null) {
                log.debug("Received RPC response: {}", message);
                Type resultType = resultTypes.get(id);

                if (resultType != null) {
                    Object result = gson.fromJson(response.getResult(), resultType);
                    callback.complete(result);
                } else {
                    callback.complete(null);
                }
            } else {
                log.error("Received RPC error: id={} code={} message={}", id, error.getCode(), error.getMessage());

                if (error.getMessage().contains("There was a problem with authentication")) {
                    callback.completeExceptionally(new SurrealAuthenticationException());
                } else if (error.getMessage().contains("There was a problem with the database: Specify a namespace to use")) {
                    callback.completeExceptionally(new SurrealNoDatabaseSelectedException());
                } else {
                    Matcher recordAlreadyExitsMatcher = RECORD_ALREADY_EXITS_PATTERN.matcher(error.getMessage());
                    if (recordAlreadyExitsMatcher.matches()) {
                        callback.completeExceptionally(new SurrealRecordAlreadyExitsException(recordAlreadyExitsMatcher.group(1), recordAlreadyExitsMatcher.group(2)));
                    } else {
                        callback.completeExceptionally(new SurrealException());
                    }
                }
            }
        } finally {
            callbacks.remove(id);
            resultTypes.remove(id);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.debug("Connection closed: code={} reason={} remote={}", code, reason, remote);
        callbacks.clear();
        resultTypes.clear();
    }

    @Override
    public void onError(Exception ex) {
        if (!(ex instanceof ConnectException) && !(ex instanceof NoRouteToHostException)) {
            log.error("Connection error", ex);
        }
    }

}
