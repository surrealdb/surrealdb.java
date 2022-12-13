package com.surrealdb.client.bidirectional;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.surrealdb.client.bidirectional.rpc.RpcRequest;
import com.surrealdb.client.bidirectional.rpc.RpcResponse;
import com.surrealdb.client.communication.CommunicationManager;
import com.surrealdb.client.communication.RequestEntry;
import com.surrealdb.client.settings.SurrealClientSettings;
import com.surrealdb.exception.SurrealConnectionTimeoutException;
import com.surrealdb.exception.SurrealExceptionUtils;
import com.surrealdb.exception.SurrealNotConnectedException;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
final class InternalWebsocketClient extends WebSocketClient {

    @NotNull ExecutorService executorService;
    @NotNull Gson gson;
    @NotNull Consumer<Boolean> onClose;

    boolean logOutgoingMessages;
    boolean logIncomingMessages;
    boolean logAuthenticationCredentials;

    @NotNull CommunicationManager communicationManager;
    @NotNull AtomicLong lastRequestId;

    InternalWebsocketClient(@NotNull SurrealClientSettings settings, @NotNull Gson gson, @NotNull Consumer<Boolean> onClose) {
        super(settings.getUri());

        this.executorService = settings.getAsyncOperationExecutorService();
        this.gson = gson;
        this.onClose = onClose;

        logOutgoingMessages = settings.isLogOutgoingMessages();
        logIncomingMessages = settings.isLogIncomingMessages();
        logAuthenticationCredentials = settings.isLogAuthenticationCredentials();

        this.communicationManager = new CommunicationManager(gson);
        this.lastRequestId = new AtomicLong();
    }

    @NotNull CompletableFuture<Void> connectAsync(long timeout, @NotNull TimeUnit timeUnit) {
        return CompletableFuture.runAsync(() -> {
            if (isOpen()) {
                log.debug("Already connected to Surreal server, connect request...");
                return;
            }

            try {
                log.debug("Connecting to Surreal server [uri: {}, timeout: {} {}]", getURI(), timeout, timeUnit);
                connectBlocking(timeout, timeUnit);
            } catch (InterruptedException ignored) {
            }

            if (!isOpen()) {
                throw new SurrealConnectionTimeoutException();
            }
        }, executorService);
    }

    @NotNull CompletableFuture<Void> disconnectAsync() {
        return CompletableFuture.runAsync(() -> {
            try {
                log.debug("Gracefully disconnecting from Surreal server");
                closeBlocking();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }


    <U> @NotNull CompletableFuture<U> rpc(@NotNull String method, @NotNull Type resultType, Object @NotNull ... arguments) {
        return CompletableFuture.supplyAsync(() -> {
            String id = Long.toString(lastRequestId.getAndIncrement());
            RpcRequest rpcRequest = new RpcRequest(id, method, arguments);

            try {
                RequestEntry<U> requestEntry = communicationManager.createRequest(id, method, resultType);
                String payloadString = gson.toJson(rpcRequest);

                logOutgoingMessage(method, id, payloadString);

                send(payloadString);
                return requestEntry.getCallback();
            } catch (Exception exception) {
                communicationManager.cancelRequest(id, exception);

                if (exception instanceof WebsocketNotConnectedException) {
                    throw new SurrealNotConnectedException();
                }

                throw SurrealExceptionUtils.wrapException(exception, "Failed to send RPC request");
            }
        }, executorService).thenCompose(Function.identity());
    }

    @Override
    public void onMessage(@NotNull String rawMessage) {
        JsonElement message = gson.fromJson(rawMessage, JsonElement.class);
        RpcResponse response = gson.fromJson(message, RpcResponse.class);

        logIncomingMessage(rawMessage);

        response.getError().ifPresentOrElse(
            error -> communicationManager.completeRequest(response.getId(), error),
            () -> communicationManager.completeRequest(response.getId(), response.getResult())
        );
    }

    private void logOutgoingMessage(@NonNull String method, @NotNull String id, @NotNull String payloadString) {
        if (!logOutgoingMessages) {
            return;
        }

        if (!logAuthenticationCredentials && method.equals("signin")) {
            log.debug("Outgoing sign in request [method: {}, id: {}]", method, id);
        } else {
            log.debug("Outgoing RPC [method: {}, data: {}", method, payloadString);
        }
    }

    private void logIncomingMessage(@NonNull String rawMessage) {
        if (!logIncomingMessages) {
            return;
        }

        log.debug("Incoming RPC [data: {}]", rawMessage);
    }

    @Override
    public void onOpen(ServerHandshake handshakeData) {
        log.debug("Connected to Surreal server '{}'", getURI());
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.debug("Disconnected from Surreal server. [code: {}, reason: {}, caused by remote: {}]", code, reason, remote);

        communicationManager.cancelAllRequests(new SurrealNotConnectedException());
        onClose.accept(remote);
    }

    @Override
    public void onError(Exception exception) {
        log.error("A WebSocket error occurred", exception);
    }
}
