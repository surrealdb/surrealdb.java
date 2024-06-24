package com.surrealdb;

import java.util.Iterator;

public class ValueIterator extends Native implements Iterator<Value> {

    ValueIterator(long ptr) {
        super(ptr);
    }

    private static native boolean hasNext(long ptr);

    private static native long next(long ptr);

    @Override
    final protected native String toString(long ptr);

    @Override
    final protected native int hashCode(long ptr);

    @Override
    final protected native boolean equals(long ptr1, long ptr2);

    @Override
    final protected native boolean deleteInstance(long ptr);

    @Override
    public boolean hasNext() {
        return hasNext(getPtr());
    }

    @Override
    public Value next() {
        return new Value(next(getPtr()));
    }
}
