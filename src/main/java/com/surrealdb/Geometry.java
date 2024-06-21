package com.surrealdb;

public class Geometry implements AutoCloseable {

    private long id;

    Geometry(long id) {
        this.id = id;
    }

    private static native boolean deleteInstance(long id);

    @Override
    public void close() {
        deleteInstance(id);
        id = 0;
    }

    @Override
    @Deprecated
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }
}

