package com.surrealdb;

import java.util.Iterator;

public class Object extends Native implements Iterable<Entry> {

    Object(long ptr) {
        super(ptr);
    }

    private static native String toString(long ptr);

    private static native String toPrettyString(long ptr);

    private static native long iterator(long ptr);

    private static native long synchronizedIterator(long ptr);

    private static native int len(long ptr);

    private static native long get(long ptr, String key);

    final protected native boolean deleteInstance(long ptr);

    public String toString() {
        return toString(getPtr());
    }

    public String toPrettyString() {
        return toPrettyString(getPtr());
    }

    public Value get(String key) {
        return new Value(get(getPtr(), key));
    }

    public int len() {
        return len(getPtr());
    }

    @Override
    public Iterator<Entry> iterator() {
        return new EntryIterator(iterator(getPtr()));
    }

    public Iterator<Entry> synchronizedIterator() {
        return new SynchronizedEntryIterator(synchronizedIterator(getPtr()));
    }

}

