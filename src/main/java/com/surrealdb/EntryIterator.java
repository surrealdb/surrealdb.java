package com.surrealdb;

import java.util.Iterator;

public class EntryIterator implements Iterator<Entry>, AutoCloseable {

    private long id;

    EntryIterator(long id) {
        this.id = id;
    }

    private static native boolean deleteInstance(long id);

    private static native boolean hasNext(long id);

    private static native long next(long id);

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

    @Override
    public boolean hasNext() {
        return hasNext(id);
    }

    @Override
    public Entry next() {
        return new Entry(next(id));
    }
}
