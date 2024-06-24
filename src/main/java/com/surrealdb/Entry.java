package com.surrealdb;

public class Entry extends Native {

    Entry(long ptr) {
        super(ptr);
    }

    private static native String getKey(long ptr);

    private static native long getValue(long ptr);

    @Override
    final protected native String toString(long ptr);

    @Override
    final protected native int hashCode(long ptr);

    @Override
    final native protected boolean equals(long ptr1, long ptr2);

    final protected native boolean deleteInstance(long ptr);

    public String getKey() {
        return getKey(getPtr());
    }

    public Value getValue() {
        return new Value(getValue(getPtr()));
    }

}

