package com.surrealdb;

import java.util.Iterator;
import java.util.Objects;

class SynchronizedValueIterator extends Native implements Iterator<Value> {

    SynchronizedValueIterator(long ptr) {
        super(ptr);
    }

    private static native boolean hasNext(long ptr);

    private static native long next(long ptr);

    @Override
    final native void deleteInstance(long ptr);

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

    @Override
    final public boolean hasNext() {
        return hasNext(getPtr());
    }

    @Override
    final public Value next() {
        return new Value(next(getPtr()));
    }
}
