package com.surrealdb;

import java.util.Iterator;

/**
 * The Object class implements the Iterable interface for Entry objects. It
 * provides methods for object manipulation and interaction.
 */
public class Object extends Native implements Iterable<Entry>, AutoCloseable {

	Object(long ptr) {
		super(ptr);
	}

	private native long iterator(long ptr);

	private native long synchronizedIterator(long ptr);

	private native int len(long ptr);

	private native long get(long ptr, String key);

	@Override
	final native String toString(long ptr);

	@Override
	final native int hashCode(long ptr);

	@Override
	final native boolean equals(long ptr1, long ptr2);

	private static native void deleteInstance(long ptr);

	@Override
	final NativeCleaner.Deleter deleter() {
		return Object::deleteInstance;
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

	final public Value get(String key) {
		return new Value(get(getPtr(), key));
	}

	final public int len() {
		return len(getPtr());
	}

	@Override
	final public Iterator<Entry> iterator() {
		return new EntryIterator(iterator(getPtr()));
	}

	final public Iterator<Entry> synchronizedIterator() {
		return new SynchronizedEntryIterator(synchronizedIterator(getPtr()));
	}

}
