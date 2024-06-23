package com.surrealdb;

import java.util.Iterator;

class SynchronizedEntryIterator extends Native implements Iterator<Entry> {

    SynchronizedEntryIterator(long ptr) {
        super(ptr);
    }

    private static native boolean hasNext(long ptr);

    private static native long next(long ptr);

    final protected native boolean deleteInstance(long ptr);

    @Override
    final public boolean hasNext() {
        return hasNext(getPtr());
    }

    @Override
    final public Entry next() {
        return new Entry(next(getPtr()));
    }
}
