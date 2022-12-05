package com.surrealdb.client.listener;

import com.google.gson.JsonElement;
import com.surrealdb.client.communication.RequestEntry;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface SurrealIncomingMessageListener<T> {

    void onMessage(@NotNull RequestEntry<T, ?> entry, @NotNull JsonElement incomingMessage);

}
