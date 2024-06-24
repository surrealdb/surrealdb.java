package com.surrealdb;

public class Thing extends Native {


    Thing(long ptr) {
        super(ptr);
    }

    public Thing(String table, long id) {
        super(newTableId(table, id));
    }

    private static native long newTableId(String table, long id);

    private static native String getTable(long ptr);

    private static native long getId(long ptr);

    @Override
    final native String toString(long ptr);

    @Override
    final native int hashCode(long ptr);

    @Override
    final native boolean equals(long ptr1, long ptr2);

    @Override
    final native boolean deleteInstance(long ptr);


    public String getTable() {
        return getTable(getPtr());
    }

    public Id getId() {
        return new Id(getId(getPtr()));
    }

}

