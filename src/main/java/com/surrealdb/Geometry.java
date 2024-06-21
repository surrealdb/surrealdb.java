package com.surrealdb;

public class Geometry implements AutoCloseable {

    private final long id;

    Geometry(long id) {
        this.id = id;
    }

    private static native boolean deleteInstance(long id);

    @Override
    public void close() {
        deleteInstance(id);
    }

}

