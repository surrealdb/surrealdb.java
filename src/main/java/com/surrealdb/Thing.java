package com.surrealdb;

public class Thing implements AutoCloseable {

    private final long id;

    Thing(long id) {
        this.id = id;
    }

    private static native boolean deleteInstance(long id);

    @Override
    public void close() {
        deleteInstance(id);
    }

}

