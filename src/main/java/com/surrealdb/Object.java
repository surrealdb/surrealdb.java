package com.surrealdb;

import java.util.Iterator;

/**
 * The Object class implements the Iterable interface for Entry objects. It
 * provides methods for object manipulation and interaction.
 */
public class Object extends Native implements Iterable<Entry> {

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
