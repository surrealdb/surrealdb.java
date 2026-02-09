package com.surrealdb;

import java.util.Optional;

/**
 * Blocking iterator over live query notifications. Call {@link #next()} in a loop and
 * {@link #close()} when done (or use try-with-resources if we add AutoCloseable).
 */
public class LiveStream {

    static {
        Loader.loadNative();
    }

    private long handle;

    LiveStream(long handle) {
        this.handle = handle;
    }

    /**
     * Blocks until the next notification is available, or the stream ends.
     *
     * @return the next notification, or empty if the stream has ended
     */
    public Optional<LiveNotification> next() {
        if (handle == 0) {
            return Optional.empty();
        }
        LiveNotification n = nextNative(handle);
        return n == null ? Optional.empty() : Optional.of(n);
    }

    /**
     * Releases the live query and stops receiving notifications. Idempotent.
     */
    public void close() {
        if (handle != 0) {
            releaseNative(handle);
            handle = 0;
        }
    }

    private static native LiveNotification nextNative(long handle);

    private static native void releaseNative(long handle);
}
