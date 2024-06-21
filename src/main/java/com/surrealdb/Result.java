package com.surrealdb;

import java.util.Iterator;

public class Result implements Iterator<Value>, AutoCloseable {

    private final long id;

    Result(long id) {
        this.id = id;
    }

    private static native void deleteInstance(long id);

    private static native boolean hasNext(long id);

    private static native long next(long id);

    @Override
    public boolean hasNext() {
        return hasNext(id);
    }

    @Override
    public Value next() {
        return new Value(next(id));
    }

    @Override
    public void close() {
        deleteInstance(id);
    }
}
