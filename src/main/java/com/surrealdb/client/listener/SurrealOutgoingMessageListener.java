package com.surrealdb.client.listener;

import com.surrealdb.client.communication.RequestEntry;
import org.jetbrains.annotations.NotNull;

public interface SurrealOutgoingMessageListener<T> {

    void onOutgoingMessage(@NotNull RequestEntry<T, ?> entry);

}
