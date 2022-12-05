package com.surrealdb.client.communication;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.surrealdb.client.listener.ListenerManager;
import com.surrealdb.client.bidirectional.rpc.RpcResponse;
import com.surrealdb.exception.SurrealException;
import com.surrealdb.exception.SurrealExceptionUtils;
import lombok.NonNull;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@ApiStatus.Internal
public final class CommunicationManager<T> {

    @NotNull Gson gson;
    @NotNull ListenerManager<T> listenerManager;
    @NotNull Map<String, RequestEntry<T, ?>> pendingRequests;

    public CommunicationManager(@NotNull Gson gson, @NotNull ListenerManager<T> listenerManager) {
        this.gson = gson;
        this.listenerManager = listenerManager;
        pendingRequests = new ConcurrentHashMap<>();
    }

    public <U> @NotNull RequestEntry<T, U> createRequest(@NotNull String id, @NotNull String method, @NotNull Type resultType, @NotNull T payload) {
        Instant timestamp = Instant.now();
        CompletableFuture<U> callback = new CompletableFuture<>();

        RequestEntry<T, U> entry = new RequestEntry<>(id, method, timestamp, payload, callback, resultType);
        pendingRequests.put(id, entry);

        listenerManager.onOutgoingMessage(entry);

        return entry;
    }

    public void completeRequest(@NotNull String id, @NotNull JsonElement data) {
        RequestEntry<T, ?> entry = getAndRemoveRequestEntry(id, "Couldn't find entry for successful request id {}");
        if (entry == null) return;

        listenerManager.onIncomingMessage(entry, data);

        CompletableFuture<?> callback = entry.getCallback();

        try {
            Type resultType = entry.getResultType();

            if (resultType.equals(Void.TYPE)) {
                callback.complete(null);
            } else {
                callback.complete(gson.fromJson(data, resultType));
            }
        } catch (Exception e) {
            RuntimeException wrappedException = SurrealExceptionUtils.wrapException("Failed to deserialize response", e);
            callback.completeExceptionally(wrappedException);
        }
    }

    public void completeRequest(@NotNull String id, @NotNull RpcResponse.Error error) {
        RequestEntry<T, ?> entry = getAndRemoveRequestEntry(id, "Couldn't find request entry for failed request id {}");
        if (entry == null) return;

        CompletableFuture<?> callback = entry.getCallback();
        String message = error.getMessage();
        SurrealException exception = SurrealExceptionUtils.createExceptionFromMessage(message);
        callback.completeExceptionally(exception);
    }

    public void cancelRequest(@NotNull String id, @NotNull Exception exception) {
        RequestEntry<?, ?> entry = getAndRemoveRequestEntry(id, "Attempted to cancel request with id {}, but no such request was found");
        if (entry == null) return;

        CompletableFuture<?> callback = entry.getCallback();
        callback.completeExceptionally(exception);
    }

    public void cancelAllRequests(@NotNull Exception exception) {
        for (RequestEntry<T, ?> entry : pendingRequests.values()) {
            CompletableFuture<?> callback = entry.getCallback();
            callback.completeExceptionally(exception);
        }
        pendingRequests.clear();
    }

    private @Nullable RequestEntry<T, ?> getAndRemoveRequestEntry(@NonNull String id, @NotNull String messageIfNotFound) {
        RequestEntry<T, ?> entry = pendingRequests.remove(id);

        if (entry == null) {
            return null;
        }

        return entry;
    }
}
