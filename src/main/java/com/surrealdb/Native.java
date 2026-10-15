package com.surrealdb;

/**
 * Base for types backed by a native pointer. Instances are only valid for the
 * lifetime of the native resource (e.g. the parent {@link Value} or connection
 * they were obtained from). Using an instance after the underlying resource has
 * been released may cause undefined behavior or native errors.
 */
public abstract class Native {

	static {
		Loader.loadNative();
	}

	// Owns the native pointer used by the native library to locate the object.
	// It is released at most once: by an explicit close, by moved() when the native
	// side takes ownership, or by the NativeCleaner once this object is
	// unreachable.
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
		handle.release();
	}

	final void moved() {
		handle.moved();
	}

	/**
	 * Keeps {@code o} reachable until this call returns. Call it after a native
	 * call that was given {@code o}'s pointer, when nothing else holds {@code o}.
	 */
	static void reachabilityFence(java.lang.Object o) {
		NativeCleaner.reachabilityFence(o);
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
		return equals(getPtr(), n2.getPtr());
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
