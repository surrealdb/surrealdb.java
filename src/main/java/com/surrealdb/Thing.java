package com.surrealdb;

public class Thing implements AutoCloseable {

    private long id;

    Thing(long id) {
        this.id = id;
    }

    private static native boolean deleteInstance(long id);

    private static native String getTable(long id);

    private static native long getId(long id);

    @Override
    public void close() {
        deleteInstance(id);
        id = 0;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    public String getTable() {
        return getTable(id);
    }

    public Id getId() {
        return new Id(getId(id));
    }
}

