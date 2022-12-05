package com.surrealdb.client.listener;

import com.google.gson.JsonElement;
import com.surrealdb.client.communication.RequestEntry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

@ApiStatus.Internal
public final class ListenerManager<T> {

    @NotNull Collection<SurrealOutgoingMessageListener<T>> outgoingMessageListeners;
    @NotNull Collection<SurrealIncomingMessageListener<T>> incomingMessageListeners;
    @NotNull Collection<SurrealExceptionListener> exceptionListeners;
    @NotNull Collection<SurrealConnectionStateChangeListener> connectionChangeListeners;
    @NotNull Collection<SurrealGenericLogListener> genericLogListeners;

    public ListenerManager() {
        outgoingMessageListeners = new ArrayList<>();
        incomingMessageListeners = new ArrayList<>();
        exceptionListeners = new ArrayList<>();
        connectionChangeListeners = new ArrayList<>();
        genericLogListeners = new ArrayList<>();
    }

    public void onOutgoingMessage(@NotNull RequestEntry<T, ?> entry) {
        for (SurrealOutgoingMessageListener<T> listener : outgoingMessageListeners) {
            listener.onOutgoingMessage(entry);
        }
    }

    public void onIncomingMessage(@NotNull RequestEntry<T, ?> entry, @NotNull JsonElement incomingMessage) {
        for (SurrealIncomingMessageListener<T> listener : incomingMessageListeners) {
            listener.onMessage(entry, incomingMessage);
        }
    }

    public void onException(@NotNull Exception e) {
        for (SurrealExceptionListener listener : exceptionListeners) {
            listener.onException(e);
        }
    }

    public void onConnectionStateChange(boolean connected, boolean changedCausedByRemote) {
        for (SurrealConnectionStateChangeListener listener : connectionChangeListeners) {
            listener.onConnectionChange(connected, changedCausedByRemote);
        }
    }

    public void onLog(@NotNull SurrealGenericLogListener.Type type, @NotNull Supplier<@NotNull String> messageSupplier) {
        if (genericLogListeners.isEmpty()) {
            return;
        }

        String message = messageSupplier.get();
        for (SurrealGenericLogListener listener : genericLogListeners) {
            listener.onLog(type, message);
        }
    }

    public void onLog(@NotNull SurrealGenericLogListener.Type type, @NotNull String message) {
        for (SurrealGenericLogListener listener : genericLogListeners) {
            listener.onLog(type, message);
        }
    }

    public void registerOutgoingMessageListener(@NotNull SurrealOutgoingMessageListener<T> listener) {
        outgoingMessageListeners.add(listener);
    }

    public void registerIncomingMessageListener(@NotNull SurrealIncomingMessageListener<T> listener) {
        incomingMessageListeners.add(listener);
    }

    public void registerExceptionListener(@NotNull SurrealExceptionListener listener) {
        exceptionListeners.add(listener);
    }

    public void registerConnectionStateChangeListener(@NotNull SurrealConnectionStateChangeListener listener) {
        connectionChangeListeners.add(listener);
    }

    public void registerLogListener(@NotNull SurrealGenericLogListener listener) {
        genericLogListeners.add(listener);
    }
}
