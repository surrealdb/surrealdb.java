package com.surrealdb;

import java.math.BigDecimal;
import java.util.UUID;

public class Value extends Native {

    Value(long ptr) {
        super(ptr);
    }

    private static native String toPrettyString(long ptr);

    private static native boolean isNone(long ptr);

    private static native boolean isNull(long ptr);

    private static native boolean isBoolean(long ptr);

    private static native boolean getBoolean(long ptr);

    private static native boolean isDouble(long ptr);

    private static native double getDouble(long ptr);

    private static native boolean isLong(long ptr);

    private static native long getLong(long ptr);

    private static native boolean isBigDecimal(long ptr);

    private static native BigDecimal getBigDecimal(long ptr);

    private static native boolean isString(long ptr);

    private static native String getString(long ptr);

    private static native boolean isUuid(long ptr);

    private static native String getUuid(long ptr);

    private static native boolean isArray(long ptr);

    private static native long getArray(long ptr);

    private static native boolean isObject(long ptr);

    private static native long getObject(long ptr);

    private static native boolean isGeometry(long ptr);

    private static native long getGeometry(long ptr);

    private static native boolean isBytes(long ptr);

    private static native byte[] getBytes(long ptr);

    private static native boolean isThing(long ptr);

    private static native long getThing(long ptr);

    @Override
    final protected native String toString(long ptr);

    @Override
    final protected native int hashCode(long ptr);

    @Override
    final protected native boolean equals(long ptr1, long ptr2);

    final protected native boolean deleteInstance(long ptr);

    public String toPrettyString() {
        return toPrettyString(getPtr());
    }

    public boolean isArray() {
        return isArray(getPtr());
    }

    public Array getArray() {
        return new Array(getArray(getPtr()));
    }

    public boolean isObject() {
        return isObject(getPtr());
    }

    public Object getObject() {
        return new Object(getObject(getPtr()));
    }

    public boolean isBoolean() {
        return isBoolean(getPtr());
    }

    public boolean getBoolean() {
        return getBoolean(getPtr());
    }

    public boolean isDouble() {
        return isDouble(getPtr());
    }

    public double getDouble() {
        return getDouble(getPtr());
    }

    public boolean isLong() {
        return isLong(getPtr());
    }

    public long getLong() {
        return getLong(getPtr());
    }

    public boolean isBigdecimal() {
        return isBigDecimal(getPtr());
    }

    public BigDecimal getBigDecimal() {
        return getBigDecimal(getPtr());
    }

    public boolean isNull() {
        return isNull(getPtr());
    }

    public boolean isNone() {
        return isNone(getPtr());
    }

    public boolean isString() {
        return isString(getPtr());
    }

    public String getString() {
        return getString(getPtr());
    }

    public boolean isUuid() {
        return isUuid(getPtr());
    }

    public UUID getUuid() {
        return UUID.fromString(getUuid(getPtr()));
    }

    public boolean isThing() {
        return isThing(getPtr());
    }

    public Thing getThing() {
        return new Thing(getThing(getPtr()));
    }

    public boolean isBytes() {
        return isBytes(getPtr());
    }

    public byte[] getBytes() {
        return getBytes(getPtr());
    }

    public boolean isGeometry() {
        return isGeometry(getPtr());
    }

    public Geometry getGeometry() {
        return new Geometry(getGeometry(getPtr()));
    }

    public <T> T get(Class<T> type) {
        return new ValueClassConverter<>(type).convert(this);
    }
}

