package com.surrealdb;

public class Id implements AutoCloseable {

    private long id;

    Id(long id) {
        this.id = id;
    }

    private static native boolean deleteInstance(long id);

    private static native boolean isLong(long id);

    private static native long getLong(long id);

    private static native boolean isString(long id);

    private static native String getString(long id);

    private static native boolean isArray(long id);

    private static native Array getArray(long id);

    private static native boolean isObject(long id);

    private static native Object getObject(long id);

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

    public boolean isLong() {
        return isLong(id);
    }

    public long getLong() {
        return getLong(id);
    }

    public boolean isString() {
        return isString(id);
    }

    public String getString() {
        return getString(id);
    }

    public boolean isArray() {
        return isArray(id);
    }

    public Array getArray() {
        return getArray(id);
    }

    public boolean isObject() {
        return isObject(id);
    }

    public Object getObject() {
        return getObject(id);
    }
}

