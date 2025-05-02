package com.surrealdb;

import java.util.UUID;

/**
 * The Id class represents a unique identifier that can be either a long value or a string.
 */
public class Id extends Native {

    Id(long ptr) {
        super(ptr);
    }

    public static Id from(long id) {
        return new Id(newLongId(id));
    }

    public static Id from(String id) {
        return new Id(newStringId(id));
    }

    public static Id from(UUID id) {
        return new Id(newUuidId(id.toString()));
    }

    private static native long newLongId(long id);

    private static native long newStringId(String id);

    private static native long newUuidId(String id);

    private static native boolean isLong(long ptr);

    private static native long getLong(long ptr);

    private static native boolean isString(long ptr);

    private static native String getString(long ptr);

    private static native boolean isUuid(long ptr);

    private static native String getUuid(long ptr);

    private static native boolean isArray(long ptr);

    private static native Array getArray(long ptr);

    private static native boolean isObject(long ptr);

    private static native Object getObject(long ptr);

    @Override
    final native int hashCode(long ptr);

    @Override
    final native String toString(long ptr);

    @Override
    final native boolean equals(long ptr1, long ptr2);

    @Override
    final native boolean deleteInstance(long ptr);

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

    final public boolean isUuid() {
        return isUuid(getPtr());
    }

    final public UUID getUuid() {
        return UUID.fromString(getUuid(getPtr()));
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

