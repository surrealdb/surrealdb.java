package com.surrealdb;

import java.util.Iterator;
import java.util.Objects;

/**
 * EntryIterator is a specialized iterator for traversing entries. This class
 * implements the Java Iterator interface to provide a seamless way to iterate
 * over Entry objects.
 */
public class EntryIterator extends Native implements Iterator<Entry> {

	EntryIterator(long ptr) {
		super(ptr);
	}

	private native boolean hasNext(long ptr);

	private native long next(long ptr);

	@Override
	final String toString(long ptr) {
		return "EntryIterator[ptr=" + ptr + "]";
	}

	@Override
	final int hashCode(long ptr) {
		return Objects.hashCode(ptr);
	}

	@Override
	final boolean equals(long ptr1, long ptr2) {
		return ptr1 == ptr2;
	}

	private static native void deleteInstance(long ptr);

	@Override
	final NativeCleaner.Deleter deleter() {
		return EntryIterator::deleteInstance;
	}

	@Override
	public boolean hasNext() {
		return hasNext(getPtr());
	}

	@Override
	public Entry next() {
		return new Entry(next(getPtr()));
	}

}
