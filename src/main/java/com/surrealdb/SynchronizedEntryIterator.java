package com.surrealdb;

import java.util.Iterator;

class SynchronizedEntryIterator extends Native implements Iterator<Entry> {

    SynchronizedEntryIterator(long ptr) {
        super(ptr);
    }

    private static native boolean hasNext(long ptr);

    private static native long next(long ptr);

    @Override
    final protected native boolean deleteInstance(long ptr);

    @Override
    final public boolean hasNext() {
        return hasNext(getPtr());
    }

    @Override
    final public Entry next() {
        return new Entry(next(getPtr()));
    }

    @Override
    final protected native String toString(long ptr);

    @Override
    final protected native int hashCode(long ptr);

    @Override
    final protected native boolean equals(long ptr1, long ptr2);

}
