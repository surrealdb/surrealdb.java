package com.surrealdb.connection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.surrealdb.connection.adapter.TemporalAdapterFactory;
import com.surrealdb.connection.exception.SurrealConnectionTimeoutException;
import com.surrealdb.connection.exception.SurrealNotConnectedException;
import com.surrealdb.connection.model.RpcRequest;
import com.surrealdb.connection.model.RpcResponse;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;

/**
 * @author Khalid Alharisi
 */
@Slf4j
public class SurrealWebSocketConnection extends WebSocketClient implements SurrealConnection {
    private final AtomicLong lastRequestId;
    private final Gson gson;
    private final Map<String, CompletableFuture<?>> callbacks;
    private final Map<String, Type> resultTypes;

    @SneakyThrows
    public SurrealWebSocketConnection(final String host, final int port, final boolean useTls) {
        super(URI.create((useTls ? "wss://" : "ws://") + host + ":" + port + "/rpc"));

        this.lastRequestId = new AtomicLong(0);
        this.gson =
                new GsonBuilder()
                        .registerTypeAdapterFactory(new TemporalAdapterFactory())
                        .disableHtmlEscaping()
                        .create();
        this.callbacks = new HashMap<>();
        this.resultTypes = new HashMap<>();
    }

    @Override
    public void connect(final int timeoutSeconds) {
        try {
            log.debug("Connecting to SurrealDB server {}", this.uri);
            this.connectBlocking(timeoutSeconds, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            throw new com.surrealdb.connection.exception.SurrealConnectionTimeoutException();
        }
        if (!this.isOpen()) {
            throw new SurrealConnectionTimeoutException();
        }
    }

    @Override
    public void disconnect() {
        try {
            this.closeBlocking();
        } catch (final InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> CompletableFuture<T> rpc(
            final Type resultType, final String method, final Object... params) {
        final RpcRequest request =
                new RpcRequest(this.lastRequestId.incrementAndGet() + "", method, params);
        final CompletableFuture<T> callback = new CompletableFuture<>();

        this.callbacks.put(request.id(), callback);
        if (resultType != null) {
            this.resultTypes.put(request.id(), resultType);
        }

        try {
            final String json = this.gson.toJson(request);
            log.debug("Sending RPC request {}", json);
            this.send(json);
        } catch (final WebsocketNotConnectedException e) {
            throw new SurrealNotConnectedException();
        }

        return callback;
    }

    @Override
    public void onOpen(final ServerHandshake handshake) {
        log.debug("Connected");
    }

    @Override
    public void onMessage(final String message) {
        final RpcResponse response = this.gson.fromJson(message, RpcResponse.class);
        final String id = response.id();
        final RpcResponse.Error error = response.error();
        final CompletableFuture<Object> callback =
                (CompletableFuture<Object>) this.callbacks.get(id);

        try {
            if (error == null) {
                log.debug("Received RPC response: {}", message);
                final Type resultType = this.resultTypes.get(id);

                if (resultType != null) {
                    Object deserialised = null;
                    final JsonElement responseElement = response.result();
                    // The protocol can sometimes send object instead of array when only 1 response
                    // is valid
                    if (responseElement.isJsonObject()) {
                        deserialised = gson.fromJson(responseElement, resultType);
                    } else if (responseElement.isJsonArray()) {
                        final JsonArray jsonArray = responseElement.getAsJsonArray();
                        deserialised = this.gson.fromJson(jsonArray, resultType);
                    } else if (responseElement.isJsonPrimitive()) {
                        final JsonPrimitive primitive = responseElement.getAsJsonPrimitive();
                        if (primitive.isNumber()) {
                            deserialised = primitive.getAsNumber().doubleValue();
                        } else if (primitive.isString()) {
                            deserialised = primitive.getAsString();
                        } else if (primitive.isBoolean()) {
                            deserialised = primitive.getAsBoolean();
                        }
                    } else if (responseElement.isJsonNull()) {
                        if (resultType.getTypeName().contains("List")) {
                            deserialised = List.of();
                        } else {
                            deserialised = null;
                        }
                    } else {
                        callback.completeExceptionally(
                                new IllegalStateException("Unhandled deserialisation case"));
                    }
                    callback.complete(deserialised);
                } else {
                    callback.complete(null);
                }
            } else {
                log.error(
                        "Received RPC error: id={} code={} message={}",
                        id,
                        error.getCode(),
                        error.getMessage());
                callback.completeExceptionally(ErrorToExceptionMapper.map(error));
            }

        } catch (final Throwable t) {
            callback.completeExceptionally(t);
        } finally {
            this.callbacks.remove(id);
            this.resultTypes.remove(id);
        }
    }

    @Override
    public void onClose(final int code, final String reason, final boolean remote) {
        log.debug("onClose");
        this.callbacks.clear();
        this.resultTypes.clear();
    }

    @Override
    public void onError(final Exception ex) {
        if (!(ex instanceof ConnectException) && !(ex instanceof NoRouteToHostException)) {
            log.error("onError", ex);
        }
    }
}
