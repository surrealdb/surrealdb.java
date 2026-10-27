package com.surrealdb;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * Frees native objects whose Java owner was garbage collected without being
 * closed. This is a small Java 8 compatible stand-in for
 * {@code java.lang.ref.Cleaner}: each {@link Native} registers a
 * {@link Handle}, a phantom reference that owns the native pointer and knows
 * how to free it without touching the (by then unreachable) owner.
 *
 * <p>
 * The pointer is cleared with an atomic {@code getAndSet(0)} by whichever comes
 * first: an explicit close, a hand-over to the native side ({@code take()}), or
 * the cleanup after GC. So these can race without freeing the pointer twice or
 * handing it over twice.
 *
 * <p>
 * Like the JDK cleaner, live handles are kept in an intrusive doubly linked
 * list, so registering and releasing a handle allocates nothing beyond the
 * handle itself.
 */
final class NativeCleaner {

	/** Frees the native object behind a pointer. Must not capture its owner. */
	interface Deleter {
		void delete(long ptr);
	}

	/** Owns one native pointer on behalf of a {@link Native}. */
	static final class Handle extends PhantomReference<Native> {

		private static final AtomicLongFieldUpdater<Handle> PTR = AtomicLongFieldUpdater.newUpdater(Handle.class,
				"ptr");

		private volatile long ptr;

		private final Deleter deleter;

		// Links in the list of live handles, guarded by LIVE. A handle that is not in
		// the list points to itself.
		private Handle prev = this;

		private Handle next = this;

		private Handle(Native owner, long ptr, Deleter deleter) {
			super(owner, QUEUE);
			this.ptr = ptr;
			this.deleter = deleter;
		}

		/** The list head: an empty handle that is never enqueued. */
		private Handle() {
			super(null, null);
			this.deleter = null;
		}

		long ptr() {
			return ptr;
		}

		/** Frees the native object, at most once. */
		void release() {
			final long p = PTR.getAndSet(this, 0);
			forget();
			if (p != 0) {
				deleter.delete(p);
			}
		}

		/**
		 * Gives up ownership and returns the pointer, for a native call that takes the
		 * object over. Returns 0 if it was already released or taken.
		 */
		long take() {
			final long p = PTR.getAndSet(this, 0);
			forget();
			return p;
		}

		private void forget() {
			synchronized (LIVE) {
				if (next != this) {
					next.prev = prev;
					prev.next = next;
					prev = this;
					next = this;
				}
			}
			clear();
		}
	}

	private static final ReferenceQueue<Native> QUEUE = new ReferenceQueue<>();

	// Keeps every handle strongly reachable until it has been released, so the
	// phantom reference is not collected before its owner is.
	private static final Handle LIVE = new Handle();

	private static final MethodHandle REACHABILITY_FENCE = findReachabilityFence();

	static {
		final Thread thread = new Thread(NativeCleaner::drain, "surrealdb-native-cleaner");
		thread.setDaemon(true);
		thread.start();
	}

	private NativeCleaner() {
	}

	static Handle register(Native owner, long ptr, Deleter deleter) {
		final Handle handle = new Handle(owner, ptr, deleter);
		try {
			if (ptr != 0) {
				synchronized (LIVE) {
					handle.prev = LIVE;
					handle.next = LIVE.next;
					LIVE.next.prev = handle;
					LIVE.next = handle;
				}
			}
		} finally {
			// If the owner became unreachable before the handle was added, the cleaner
			// could release it first and the add would then keep it forever.
			reachabilityFence(owner);
		}
		return handle;
	}

	/**
	 * Keeps {@code o} strongly reachable up to this call, so that its native object
	 * cannot be freed by the cleaner while a native call that received its pointer
	 * is still running. Uses {@code Reference.reachabilityFence} on Java 9+, and a
	 * synchronized block on Java 8.
	 */
	static void reachabilityFence(java.lang.Object o) {
		if (REACHABILITY_FENCE != null) {
			try {
				REACHABILITY_FENCE.invokeExact(o);
				return;
			} catch (Throwable t) {
				// Fall through to the Java 8 approach.
			}
		}
		synchronized (o) {
			// An empty synchronized block on o keeps it reachable up to here.
		}
	}

	private static MethodHandle findReachabilityFence() {
		try {
			return MethodHandles.lookup().findStatic(Reference.class, "reachabilityFence",
					MethodType.methodType(void.class, java.lang.Object.class));
		} catch (ReflectiveOperationException | RuntimeException e) {
			return null;
		}
	}

	private static void drain() {
		while (true) {
			try {
				((Handle) QUEUE.remove()).release();
			} catch (InterruptedException e) {
				// Daemon thread: keep draining.
			} catch (Throwable t) {
				// A failing deleter must not stop the cleaner.
			}
		}
	}
}
