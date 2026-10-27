package com.surrealdb;

/**
 * Entry represents a key-value pair entry in a native data structure.
 * <p>
 * Entry provides methods to retrieve the key and value associated with the
 * entry.
 */
public class Entry extends Native implements AutoCloseable {

	Entry(long ptr) {
		super(ptr);
	}

	private native String getKey(long ptr);

	private native long getValue(long ptr);

	@Override
	final native String toString(long ptr);

	@Override
	final native int hashCode(long ptr);

	@Override
	final native boolean equals(long ptr1, long ptr2);

	private static native void deleteInstance(long ptr);

	@Override
	final NativeCleaner.Deleter deleter() {
		return Entry::deleteInstance;
	}

	public String getKey() {
		return getKey(getPtr());
	}

	public Value getValue() {
		return new Value(getValue(getPtr()));
	}

}
