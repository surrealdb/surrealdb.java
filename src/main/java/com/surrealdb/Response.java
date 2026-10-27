package com.surrealdb;

import java.util.Objects;

/**
 * The Response class is a specialized wrapper for handling responses from
 * SurrealDB.
 */
public class Response extends Native implements AutoCloseable {

	Response(long ptr) {
		super(ptr);
	}

	private native int size(long ptr);

	private static native void deleteInstance(long ptr);

	@Override
	final NativeCleaner.Deleter deleter() {
		return Response::deleteInstance;
	}

	/**
	 * Releases the native memory held by this instance. Calling it more than once
	 * is safe, and an instance that is never closed is still released once it is
	 * garbage collected. After this call, using the instance throws a
	 * {@link NullPointerException}.
	 * <p>
	 * Values obtained from this instance (for example through {@code take},
	 * {@code get}, {@code getArray()} or an iterator) are not affected: they hold
	 * their own reference to the data, which is freed once every holder is closed
	 * or collected.
	 */
	@Override
	public void close() {
		deleteInstance();
	}

	private native long take(long ptr, int num);

	public Value take(int num) {
		return new Value(take(getPtr(), num));
	}

	public <T> T take(Class<T> type, int num) {
		return take(num).get(type);
	}

	public int size() {
		return size(getPtr());
	}

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

}
