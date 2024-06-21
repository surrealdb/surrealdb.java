package com.surrealdb;

public class Entry implements AutoCloseable {

    private final long id;

    Entry(long id) {
        this.id = id;
    }

    private static native boolean deleteInstance(long id);

    private static native String getKey(long id);

    private static native long getValue(long id);

    public String getKey() {
        return getKey(id);
    }

    public Value getValue() {
        return new Value(getValue(id));
    }

    @Override
    public void close() {
        deleteInstance(id);
    }

}

