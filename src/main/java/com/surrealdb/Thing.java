package com.surrealdb;

public class Thing extends Native {


    Thing(long ptr) {
        super(ptr);
    }

    private static native long newTableId(String table, long id);

    private static native String getTable(long ptr);

    private static native boolean equals(long ptr1, long ptr2);

    private static native int hashCode(long ptr);

    private static native long getId(long ptr);

    final protected native boolean deleteInstance(long ptr);

    public Thing(String table, long id) {
        super(newTableId(table, id));
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Thing thg = (Thing) o;
        return equals(getPtr(), thg.getPtr());
    }

    @Override
    public int hashCode() {
        return hashCode(getPtr());
    }

    public String getTable() {
        return getTable(getPtr());
    }

    public Id getId() {
        return new Id(getId(getPtr()));
    }
}

