package com.surrealdb;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * Base for types backed by a native pointer. Each instance owns its native
 * object: the native side clones a shared reference or copies the data when it
 * creates the instance, so it stays valid on its own, independently of the
 * {@link Value} or connection it was obtained from. The native object is freed
 * when the instance is garbage collected.
 */
public abstract class Native {

	static {
		Loader.loadNative();
	}

	// Owns the native pointer used by the native library to locate the object.
	// It is released at most once: by an explicit close, by takePtr() when the
	// native side takes ownership, or by the NativeCleaner once this object is
	// unreachable.
	//
	// A native call that is given getPtr() must keep this object reachable until
	// it returns, or the cleaner could free the pointer mid-call. For the receiver
	// this is done by declaring such natives as instance methods: JNI holds the
	// receiver for the duration of the call. (The Rust side ignores that
	// argument, so it still declares it as a JClass.) Any other Native whose
	// pointer is passed must be kept alive with reachabilityFence().
	private final NativeCleaner.Handle handle;

	Native(long ptr) {
		this.handle = NativeCleaner.register(this, ptr, deleter());
	}

	/**
	 * Returns the function that frees this type's native object. It must be a
	 * static method reference: the cleaner calls it after this object is gone.
	 */
	abstract NativeCleaner.Deleter deleter();

	abstract String toString(long ptr);

	abstract int hashCode(long ptr);

	abstract boolean equals(long ptr1, long ptr2);

	final long getPtr() {
		return handle.ptr();
	}

	final void deleteInstance() {
		try {
			handle.release();
		} finally {
			// Keep this object reachable until the release has completed, so a
			// try-with-resources block around a native call cannot lose it early.
			reachabilityFence(this);
		}
	}

	/**
	 * Releases the native resources held by this instance right away, instead of
	 * when it is garbage collected. For a {@link Surreal}, this also closes its
	 * connection. Calling it more than once is safe. After this call, using the
	 * instance throws a {@link NullPointerException}.
	 * <p>
	 * Instances obtained from this one (a value taken from a {@link Response}, an
	 * element of an {@link Array}, a field of an {@link Object}, an entry's value,
	 * and so on) are not affected: each holds its own reference to the data, which
	 * is freed once every holder is closed or collected.
	 */
	public void close() {
		deleteInstance();
	}

	/**
	 * Gives up ownership of the native object and returns its pointer, for a native
	 * call that takes the object over (and frees it, even when the call fails). The
	 * pointer is cleared before the call, so two threads handing over the same
	 * object cannot both pass it on: the second one gets 0, which the native side
	 * rejects.
	 */
	final long takePtr() {
		try {
			return handle.take();
		} finally {
			// Until the pointer is cleared, the cleaner must not see this object as
			// unreachable: it would free a pointer the native side is about to own.
			reachabilityFence(this);
		}
	}

	/**
	 * {@link #takePtr()} for every element of {@code natives}, read once. A
	 * {@code null} element, or an element that appears twice, is rejected before
	 * anything is taken: the native side would otherwise get a 0 pointer for it and
	 * fail with a less helpful error, after the earlier elements were already
	 * handed over.
	 */
	static long[] takePtrs(List<? extends Native> natives) {
		final Native[] array = natives.toArray(new Native[0]);
		// Validate everything before taking anything: a failure must not leave
		// the earlier elements taken from their owners and never handed over.
		for (final Native n : array) {
			java.util.Objects.requireNonNull(n, "A Native element cannot be null");
		}
		if (array.length > 1) {
			final Set<Native> seen = Collections.newSetFromMap(new IdentityHashMap<Native, Boolean>(array.length));
			for (final Native n : array) {
				if (!seen.add(n)) {
					throw new IllegalArgumentException(
							"The same " + n.getClass().getSimpleName() + " was passed more than once");
				}
			}
		}
		final long[] ptrs = new long[array.length];
		for (int i = 0; i < array.length; i++) {
			ptrs[i] = array[i].takePtr();
		}
		return ptrs;
	}

	/**
	 * The pointer of every element of {@code natives}, without taking ownership.
	 * The caller must keep the array reachable (see {@link #reachabilityFence})
	 * until the native call that received the pointers has returned.
	 */
	static long[] getPtrs(Native[] natives) {
		final long[] ptrs = new long[natives.length];
		for (int i = 0; i < natives.length; i++) {
			ptrs[i] = natives[i].getPtr();
		}
		return ptrs;
	}

	/**
	 * Keeps {@code o} reachable until this call returns. Call it after a native
	 * call that was given {@code o}'s pointer, when nothing else holds {@code o}.
	 * Does nothing for {@code null}.
	 */
	static void reachabilityFence(java.lang.Object o) {
		if (o != null) {
			NativeCleaner.reachabilityFence(o);
		}
	}

	@Override
	final public boolean equals(java.lang.Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		final Native n2 = (Native) o;
		try {
			return equals(getPtr(), n2.getPtr());
		} finally {
			reachabilityFence(n2);
		}
	}

	@Override
	final public int hashCode() {
		return hashCode(getPtr());
	}

	@Override
	final public String toString() {
		return toString(getPtr());
	}
}
