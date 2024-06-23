package com.surrealdb;

public class Thing extends Native {


    Thing(long ptr) {
        super(ptr);
    }

    private static native String getTable(long ptr);

    private static native long getId(long ptr);

    final protected native boolean deleteInstance(long ptr);

    public String getTable() {
        return getTable(getPtr());
    }

    public Id getId() {
        return new Id(getId(getPtr()));
    }
}

