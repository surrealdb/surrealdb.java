package com.surrealdb;

public abstract class Native {

    // Unique internal ptr used by the native library to locate the SurrealDB instance
    private long ptr;

    protected Native(long ptr) {
        this.ptr = ptr;
    }

    protected abstract boolean deleteInstance(long ptr);

    final protected long getPtr() {
        return this.ptr;
    }

    final protected boolean deleteInstance() {
        final boolean b = deleteInstance(ptr);
        ptr = 0;
        return b;
    }

    final protected void moved() {
        this.ptr = 0;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            deleteInstance();
        } finally {
            super.finalize();
        }
    }
}
