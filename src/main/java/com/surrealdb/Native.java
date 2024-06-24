package com.surrealdb;

public abstract class Native {

    // Unique internal ptr used by the native library to locate the SurrealDB instance
    private long ptr;

    protected Native(long ptr) {
        this.ptr = ptr;
    }

    protected abstract String toString(long ptr);

    protected abstract int hashCode(long ptr);

    protected abstract boolean equals(long ptr1, long ptr2);

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
    final public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Native n2 = (Native) o;
        return equals(ptr, n2.ptr);
    }

    @Override
    final public int hashCode() {
        return hashCode(ptr);
    }

    @Override
    final public String toString() {
        return toString(ptr);
    }

    @Override
    final protected void finalize() throws Throwable {
        try {
            deleteInstance();
        } finally {
            super.finalize();
        }
    }
}
