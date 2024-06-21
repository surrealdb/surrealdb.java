package com.surrealdb;

import java.math.BigDecimal;
import java.util.UUID;

public class Value implements AutoCloseable {

    private long id;

    Value(long id) {
        this.id = id;
    }

    private static native boolean deleteInstance(long id);

    private static native String toString(long id);

    private static native String toPrettyString(long id);

    private static native boolean isNone(long id);

    private static native boolean isNull(long id);

    private static native boolean isBoolean(long id);

    private static native boolean getBoolean(long id);

    private static native boolean isDouble(long id);

    private static native double getDouble(long id);

    private static native boolean isLong(long id);

    private static native long getLong(long id);

    private static native boolean isBigDecimal(long id);

    private static native BigDecimal getBigDecimal(long id);

    private static native boolean isString(long id);

    private static native String getString(long id);

    private static native boolean isUuid(long id);

    private static native UUID getUuid(long id);

    private static native boolean isArray(long id);

    private static native long getArray(long id);

    private static native boolean isObject(long id);

    private static native long getObject(long id);

    private static native boolean isGeometry(long id);

    private static native long getGeometry(long id);

    private static native boolean isBytes(long id);

    private static native byte[] getBytes(long id);

    private static native boolean isThing(long id);

    private static native long getThing(long id);

    public String toString() {
        return toString(id);
    }

    public String toPrettyString() {
        return toPrettyString(id);
    }

    public boolean isArray() {
        return isArray(id);
    }

    public Array getArray() {
        return new Array(getArray(id));
    }

    public boolean isObject() {
        return isObject(id);
    }

    public Object getObject() {
        return new Object(getObject(id));
    }

    public boolean isBoolean() {
        return isBoolean(id);
    }

    public boolean getBoolean() {
        return getBoolean(id);
    }

    public boolean isDouble() {
        return isDouble(id);
    }

    public double getDouble() {
        return getDouble(id);
    }

    public boolean isLong() {
        return isLong(id);
    }

    public float getLong() {
        return getLong(id);
    }

    public boolean isBigdecimal() {
        return isBigDecimal(id);
    }

    public BigDecimal getBigDecimal() {
        return getBigDecimal(id);
    }

    public boolean isNull() {
        return isNull(id);
    }

    public boolean isNone() {
        return isNone(id);
    }

    public boolean isString() {
        return isString(id);
    }

    public String getString() {
        return getString(id);
    }

    public boolean isUuid() {
        return isUuid(id);
    }

    public UUID getUuid() {
        return getUuid(id);
    }

    public boolean isThing() {
        return isThing(id);
    }

    public Thing getThing() {
        return new Thing(getThing(id));
    }

    public boolean isBytes() {
        return isBytes(id);
    }

    public byte[] getBytes() {
        return getBytes(id);
    }

    public boolean isGeometry() {
        return isGeometry(id);
    }

    public Geometry getGeometry() {
        return new Geometry(getGeometry(id));
    }

    @Override
    public void close() {
        deleteInstance(id);
        id = 0;
    }

    @Override
    @Deprecated
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }
}

