package com.surrealdb;

import java.util.Iterator;

public class SynchronizedValueIterator extends Native implements Iterator<Value> {
    
    SynchronizedValueIterator(long ptr) {
        super(ptr);
    }

    private static native boolean hasNext(long ptr);

    private static native long next(long ptr);

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
