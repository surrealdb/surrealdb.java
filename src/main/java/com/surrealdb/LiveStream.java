package com.surrealdb;

import java.util.Optional;

/**
 * Blocking iterator over live query notifications returned by
 * {@link Surreal#selectLive(String)}.
 *
 * <p>
 * Typical usage:
 *
 * <pre>{@code
 * try (LiveStream stream = surreal.selectLive("person")) {
 * 	while (true) {
 * 		Optional<LiveNotification> n = stream.next();
 * 		if (!n.isPresent())
 * 			break; // stream closed
 * 		process(n.get());
 * 	}
 * }
 * }</pre>
 *
 * <p>
 * <b>Thread safety:</b> {@link #next()} may be called from one thread while
 * {@link #close()} is called from another. The {@code close()} call will
 * unblock any thread currently waiting inside {@code next()}. The native handle
 * is declared {@code volatile} so that the zeroing performed by {@code close()}
 * is immediately visible to concurrent {@code next()} callers. Concurrent calls
 * to {@code next()} from multiple threads are serialized by a mutex in the
 * native layer.
 */
public class LiveStream implements AutoCloseable {

	/**
	 * Pointer to the native {@code LiveStreamChannel}. Zeroed by {@link #close()}
	 * after the native resources have been released. Declared {@code volatile} so
	 * that a {@code close()} on one thread is visible to a concurrent
	 * {@code next()} on another thread.
	 */
	private volatile long handle;

	/**
	 * The live query UUID. Available immediately (before the first notification)
	 * and unaffected by {@link #close()}.
	 */
	private final String queryId;

	LiveStream(long handle, String queryId) {
		this.handle = handle;
		this.queryId = queryId;
	}

	/**
	 * Blocks until the next notification is available, or the stream ends.
	 *
	 * <p>
	 * Returns {@link Optional#empty()} when the stream has been closed (either
	 * explicitly via {@link #close()} or because the server ended the live query).
	 * If the underlying live query encounters an error, a {@link SurrealException}
	 * is thrown.
	 *
	 * @return the next notification, or empty if the stream has ended
	 * @throws SurrealException
	 *             if the live query encounters an error
	 */
	public Optional<LiveNotification> next() {
		if (handle == 0) {
			return Optional.empty();
		}
		LiveNotification n = nextNative(handle);
		return n == null ? Optional.empty() : Optional.of(n);
	}

	/**
	 * Releases the live query and stops receiving notifications.
	 *
	 * <p>
	 * If another thread is blocked inside {@link #next()}, it will be unblocked and
	 * will return {@link Optional#empty()}. This method is idempotent: calling it
	 * more than once has no effect.
	 */
	@Override
	public void close() {
		if (handle != 0) {
			releaseNative(handle);
			handle = 0;
		}
	}

	/**
	 * Returns the UUID of this live query.
	 *
	 * <p>
	 * The id is available immediately after {@link Surreal#selectLive(String)}
	 * returns — before any notification arrives — and can be passed to
	 * {@link Surreal#kill(String)} to stop the live query. It is also carried by
	 * every {@link LiveNotification} (see {@link LiveNotification#getQueryId()}).
	 *
	 * @return the live query UUID as a string
	 */
	public String getQueryId() {
		return queryId;
	}

	private static native LiveNotification nextNative(long handle);

	private static native void releaseNative(long handle);
}
