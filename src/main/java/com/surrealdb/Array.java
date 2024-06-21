package com.surrealdb;

public class Array implements AutoCloseable {

    private long id;

    Array(long id) {
        this.id = id;
    }

    private static native boolean deleteInstance(long id);

    private static native String toString(long id);

    private static native String toPrettyString(long id);

    private static native long get(long id, int idx);

    private static native int len(long id);

    private static native long iter(long id);

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

    public String toString() {
        return toString(id);
    }

    public String toPrettyString() {
        return toPrettyString(id);
    }

    public Value get(int idx) {
        return new Value(get(id, idx));
    }

    public int len() {
        return len(id);
    }

    public ValueIterator iter() {
        return new ValueIterator(iter(id));
    }
}

