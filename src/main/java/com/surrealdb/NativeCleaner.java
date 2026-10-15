package com.surrealdb;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.ref.PhantomReference;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Frees native objects whose Java owner was garbage collected without being
 * closed. This is a small Java 8 compatible stand-in for
 * {@code java.lang.ref.Cleaner}: each {@link Native} registers a
 * {@link Handle}, a phantom reference that owns the native pointer and knows
 * how to free it without touching the (by then unreachable) owner.
 *
 * <p>
 * The pointer lives in an {@link AtomicLong} and is released with
 * {@code getAndSet(0)}, so an explicit close, a GC-driven cleanup and a
 * {@code moved()} hand-over can race without double-freeing.
 */
final class NativeCleaner {

	/** Frees the native object behind a pointer. Must not capture its owner. */
	interface Deleter {
		void delete(long ptr);
	}

	/** Owns one native pointer on behalf of a {@link Native}. */
	static final class Handle extends PhantomReference<Native> {

		private final AtomicLong ptr;

		private final Deleter deleter;

		private Handle(Native owner, long ptr, Deleter deleter) {
			super(owner, QUEUE);
			this.ptr = new AtomicLong(ptr);
			this.deleter = deleter;
		}

		long ptr() {
			return ptr.get();
		}

		/** Frees the native object, at most once. */
		void release() {
			final long p = ptr.getAndSet(0);
			forget();
			if (p != 0) {
				deleter.delete(p);
			}
		}

		/** Gives up ownership without freeing (the native side took it over). */
		void moved() {
			ptr.set(0);
			forget();
		}

		private void forget() {
			LIVE.remove(this);
			clear();
		}
	}

	private static final ReferenceQueue<Native> QUEUE = new ReferenceQueue<>();

	// Keeps every handle strongly reachable until it has been released, so the
	// phantom reference is not collected before its owner is.
	private static final Set<Handle> LIVE = Collections.newSetFromMap(new ConcurrentHashMap<Handle, Boolean>());

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
		if (ptr != 0) {
			LIVE.add(handle);
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
