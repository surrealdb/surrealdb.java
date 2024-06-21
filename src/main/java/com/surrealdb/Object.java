package com.surrealdb;

public class Object implements AutoCloseable {

    private final long id;

    Object(long id) {
        this.id = id;
    }

    private static native boolean deleteInstance(long id);

    private static native long iter(long id);

    private static native long get(long id);

    public Value get(String key) {
        return new Value(get(id));
    }

    public EntryIterator iter() {
        return new EntryIterator(get(id));
    }

    @Override
    public void close() {
        deleteInstance(id);
    }


}

