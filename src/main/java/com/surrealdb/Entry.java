package com.surrealdb;

public class Entry extends Native {

    Entry(long ptr) {
        super(ptr);
    }

    private static native String getKey(long ptr);

    private static native long getValue(long ptr);

    final protected native boolean deleteInstance(long ptr);

    public String getKey() {
        return getKey(getPtr());
    }

    public Value getValue() {
        return new Value(getValue(getPtr()));
    }

}

