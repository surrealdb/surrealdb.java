package com.surrealdb.client.communication;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.surrealdb.client.bidirectional.rpc.RpcResponse;
import com.surrealdb.exception.SurrealException;
import com.surrealdb.exception.SurrealExceptionUtils;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@ApiStatus.Internal
@Slf4j
public final class CommunicationManager {

    @NotNull Gson gson;
    @NotNull Map<String, RequestEntry<?>> pendingRequests;

    public CommunicationManager(@NotNull Gson gson) {
        this.gson = gson;
        pendingRequests = new ConcurrentHashMap<>();
    }

    public <T> @NotNull RequestEntry<T> createRequest(@NotNull String id, @NotNull String method, @NotNull Type resultType) {
        Instant timestamp = Instant.now();
        CompletableFuture<T> callback = new CompletableFuture<>();
        RequestEntry<T> entry = new RequestEntry<>(id, method, timestamp, callback, resultType);
        pendingRequests.put(id, entry);
        return entry;
    }

    public void completeRequest(@NotNull String id, @NotNull JsonElement data) {
        RequestEntry<?> entry = getAndRemoveRequestEntry(id, "Couldn't find entry for successful request id {}");
        CompletableFuture<?> callback = entry.getCallback();

        try {
            Type resultType = entry.getResultType();

            if (resultType.equals(Void.TYPE)) {
                callback.complete(null);
            } else {
                callback.complete(gson.fromJson(data, resultType));
            }
        } catch (Exception e) {
            RuntimeException wrappedException = SurrealExceptionUtils.wrapException(e, "Failed to deserialize response");
            callback.completeExceptionally(wrappedException);
        }
    }

    public void completeRequest(@NotNull String id, @NotNull RpcResponse.Error error) {
        RequestEntry<?> entry = getAndRemoveRequestEntry(id, "Couldn't find request entry for failed request id {}");
        CompletableFuture<?> callback = entry.getCallback();
        String message = error.getMessage();
        SurrealException exception = SurrealExceptionUtils.createExceptionFromMessage(message);
        callback.completeExceptionally(exception);
    }

    public void cancelRequest(@NotNull String id, @NotNull Exception exception) {
        RequestEntry<?> entry = getAndRemoveRequestEntry(id, "Attempted to cancel request with id {}, but no such request was found");

        CompletableFuture<?> callback = entry.getCallback();
        callback.completeExceptionally(exception);
    }

    public void cancelAllRequests(@NotNull Exception exception) {
        for (RequestEntry<?> entry : pendingRequests.values()) {
            CompletableFuture<?> callback = entry.getCallback();
            callback.completeExceptionally(exception);
        }
        pendingRequests.clear();
    }

    private @NotNull RequestEntry<?> getAndRemoveRequestEntry(@NonNull String id, @NotNull String messageIfNotFound) {
        RequestEntry<?> entry = pendingRequests.remove(id);

        if (entry == null) {
            throw new IllegalStateException(messageIfNotFound);
        }

        return entry;
    }
}
