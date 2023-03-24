package com.surrealdb.connection;

import com.google.gson.*;
import com.surrealdb.connection.exception.SurrealConnectionTimeoutException;
import com.surrealdb.connection.exception.SurrealNotConnectedException;
import com.surrealdb.connection.model.RpcRequest;
import com.surrealdb.connection.model.RpcResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;

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
    public SurrealWebSocketConnection(String host, int port, boolean useTls) {
        super(URI.create((useTls ? "wss://" : "ws://") + host + ":" + port + "/rpc"));

        this.lastRequestId = new AtomicLong(0);
        this.gson = new GsonBuilder().disableHtmlEscaping().create();
        this.callbacks = new HashMap<>();
        this.resultTypes = new HashMap<>();
    }

    @Override
    public void connect(int timeoutSeconds) {
        try {
            log.debug("Connecting to SurrealDB server {}", uri);
            this.connectBlocking(timeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new com.surrealdb.connection.exception.SurrealConnectionTimeoutException();
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
            log.debug("Sending RPC request {}", json);
            send(json);
        } catch (WebsocketNotConnectedException e) {
            throw new SurrealNotConnectedException();
        }

        return callback;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        log.debug("Connected");
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
                    Object deserialised = null;
                    JsonElement responseElement = response.getResult();
                    // The protocol can sometimes send object instead of array when only 1 response is valid
                    if (responseElement.isJsonObject()) {
                        JsonArray jsonArray = new JsonArray(1);
                        jsonArray.add(responseElement);
                        deserialised = gson.fromJson(jsonArray, resultType);
                    } else if (responseElement.isJsonArray()) {
                        JsonArray jsonArray = responseElement.getAsJsonArray();
                        deserialised = gson.fromJson(jsonArray, resultType);
                    } else if (responseElement.isJsonPrimitive()) {
                        JsonPrimitive primitive = responseElement.getAsJsonPrimitive();
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
                        callback.completeExceptionally(new IllegalStateException("Unhandled deserialisation case"));
                    }
                    callback.complete(deserialised);
                } else {
                    callback.complete(null);
                }
            } else {
                log.error("Received RPC error: id={} code={} message={}", id, error.getCode(), error.getMessage());
                callback.completeExceptionally(ErrorToExceptionMapper.map(error));
            }

        } catch (Throwable t) {
            callback.completeExceptionally(t);
        } finally {
            callbacks.remove(id);
            resultTypes.remove(id);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.debug("onClose");
        callbacks.clear();
        resultTypes.clear();
    }

    @Override
    public void onError(Exception ex) {
        if (!(ex instanceof ConnectException) && !(ex instanceof NoRouteToHostException)) {
            log.error("onError", ex);
        }
    }

}
