package com.surrealdb;

import java.util.Iterator;
import java.util.Objects;

class SynchronizedEntryIterator extends Native implements Iterator<Entry> {

    SynchronizedEntryIterator(long ptr) {
        super(ptr);
    }

    private static native boolean hasNext(long ptr);

    private static native long next(long ptr);

    @Override
    final native void deleteInstance(long ptr);

    @Override
    final public boolean hasNext() {
        return hasNext(getPtr());
    }

    @Override
    final public Entry next() {
        return new Entry(next(getPtr()));
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
