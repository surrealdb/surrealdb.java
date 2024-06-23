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

    final protected native boolean deleteInstance(long ptr);

    public boolean isLong() {
        return isLong(getPtr());
    }

    public long getLong() {
        return getLong(getPtr());
    }

    public boolean isString() {
        return isString(getPtr());
    }

    public String getString() {
        return getString(getPtr());
    }

    public boolean isArray() {
        return isArray(getPtr());
    }

    public Array getArray() {
        return getArray(getPtr());
    }

    public boolean isObject() {
        return isObject(getPtr());
    }

    public Object getObject() {
        return getObject(getPtr());
    }
}

