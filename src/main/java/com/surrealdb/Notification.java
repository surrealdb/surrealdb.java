package com.surrealdb;

import java.util.Objects;
import java.util.UUID;

/**
 * Wrapper for a live query notification.
 *
 * @param <T> the desired Java type of the payload
 */
public class Notification<T> extends Native {

    private final Class<T> type;

    Notification(Class<T> type, long ptr) {
        super(ptr);
        this.type = type;
    }

    private static native String getQueryId(long ptr);

    private static native int getActionCode(long ptr);

    private static native long getData(long ptr);

    @Override
    final native void deleteInstance(long ptr);

    @Override
    final String toString(long ptr) {
        return getClass().getName() + "[ptr=" + ptr + "]";
    }

    @Override
    final int hashCode(long ptr) {
        return Objects.hashCode(ptr);
    }

    @Override
    final boolean equals(long ptr1, long ptr2) {
        return ptr1 == ptr2;
    }

    public UUID getQueryId() {
        return UUID.fromString(getQueryId(getPtr()));
    }

    public Action getAction() {
        return Action.fromCode(getActionCode(getPtr()));
    }

    public Value getDataValue() {
        return new Value(getData(getPtr()));
    }

    public T getData() {
        return new Value(getData(getPtr())).get(type);
    }
}
