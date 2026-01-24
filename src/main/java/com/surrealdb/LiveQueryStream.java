package com.surrealdb;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;

/**
 * A blocking live query stream with optional timed polling support.
 *
 * @param <T> the desired Java type of the notification payload
 */
public class LiveQueryStream<T> extends Native implements AutoCloseable {

    private final Class<T> type;

    LiveQueryStream(Class<T> type, long ptr) {
        super(ptr);
        this.type = type;
    }

    private static native long next(long ptr);

    private static native long pollNext(long ptr, long timeoutMillis);

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

    /**
     * Blocks until the next notification is available or the stream ends.
     *
     * @return the next notification, or {@code null} if the stream has ended
     */
    public Notification<T> next() {
        long notificationPtr = next(getPtr());
        if (notificationPtr == 0) {
            return null;
        }
        return new Notification<>(type, notificationPtr);
    }

    /**
     * Waits up to the given timeout for the next notification.
     *
     * @param timeout maximum time to wait; non-positive values trigger an immediate check
     * @return an optional notification if one arrived before the timeout
     */
    public Optional<Notification<T>> pollNext(Duration timeout) {
        Objects.requireNonNull(timeout, "timeout");
        long millis = timeout.toMillis();
        long notificationPtr = pollNext(getPtr(), millis);
        if (notificationPtr == 0) {
            return Optional.empty();
        }
        return Optional.of(new Notification<>(type, notificationPtr));
    }

    @Override
    public void close() {
        deleteInstance();
    }
}
