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
    final protected native boolean deleteInstance(long ptr);

    @Override
    protected String toString(long ptr) {
        return getClass().getName() + "[ptr=" + ptr + "]";
    }

    @Override
    protected int hashCode(long ptr) {
        return Objects.hashCode(ptr);
    }

    @Override
    protected boolean equals(long ptr1, long ptr2) {
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
