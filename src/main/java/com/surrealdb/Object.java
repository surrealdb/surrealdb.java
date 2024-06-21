package com.surrealdb;

public class Object implements AutoCloseable {

    private long id;

    Object(long id) {
        this.id = id;
    }

    private static native boolean deleteInstance(long id);

    private static native String toString(long id);

    private static native String toPrettyString(long id);

    private static native long iter(long id);

    private static native int len(long id);

    private static native long get(long id, String key);

    public String toString() {
        return toString(id);
    }

    public String toPrettyString() {
        return toPrettyString(id);
    }
    
    public Value get(String key) {
        return new Value(get(id, key));
    }

    public int len() {
        return len(id);
    }

    public EntryIterator iter() {
        return new EntryIterator(iter(id));
    }

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

