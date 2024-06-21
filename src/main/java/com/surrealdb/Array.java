package com.surrealdb;

public class Array implements AutoCloseable {

    private final long id;

    Array(long id) {
        this.id = id;
    }

    private static native boolean deleteInstance(long id);

    private static native long iter(long id);

    @Override
    public void close() {
        deleteInstance(id);
    }

    public ValueIterator iter() {
        return new ValueIterator(iter(id));
    }
}

