package com.surrealdb;

public class Id extends Native {

    Id(long ptr) {
        super(ptr);
    }

    private static native boolean isLong(long ptr);

    private static native long getLong(long ptr);

    private static native boolean isString(long ptr);

    private static native String getString(long ptr);

    private static native boolean isArray(long ptr);

    private static native Array getArray(long ptr);

    private static native boolean isObject(long ptr);

    private static native Object getObject(long ptr);

    @Override
    final protected native int hashCode(long ptr);

    @Override
    final protected native String toString(long ptr);

    @Override
    final protected native boolean equals(long ptr1, long ptr2);

    final protected native boolean deleteInstance(long ptr);

    final public boolean isLong() {
        return isLong(getPtr());
    }

    final public long getLong() {
        return getLong(getPtr());
    }

    final public boolean isString() {
        return isString(getPtr());
    }

    final public String getString() {
        return getString(getPtr());
    }

    final public boolean isArray() {
        return isArray(getPtr());
    }

    final public Array getArray() {
        return getArray(getPtr());
    }

    final public boolean isObject() {
        return isObject(getPtr());
    }

    final public Object getObject() {
        return getObject(getPtr());
    }

}

