package com.surrealdb;

import java.util.Iterator;

/**
 * The Object class implements the Iterable interface for Entry objects.
 * It provides methods for object manipulation and interaction.
 */
public class Object extends Native implements Iterable<Entry> {

    Object(long ptr) {
        super(ptr);
    }

    private static native String toPrettyString(long ptr);

    private static native long iterator(long ptr);

    private static native long synchronizedIterator(long ptr);

    private static native int len(long ptr);

    private static native long get(long ptr, String key);

    @Override
    final native String toString(long ptr);

    @Override
    final native int hashCode(long ptr);

    @Override
    final native boolean equals(long ptr1, long ptr2);

    @Override
    final native boolean deleteInstance(long ptr);

    final public String toPrettyString() {
        return toPrettyString(getPtr());
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

