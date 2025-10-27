package com.surrealdb;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;

public class ValueMut extends Native {

    private ValueMut(long ptr) {
        super(ptr);
    }

    private static native long newNone();

    private static native long newNull();

    private static native long newString(String s);

    private static native long newBoolean(boolean b);

    private static native long newDouble(double d);

    private static native long newLong(long l);

    private static native long newDecimal(String s);

    private static native long newDuration(long l);

    private static native long newDatetime(long seconds, long nanos);

    private static native long newUuid(String s);

    private static native long newId(long ptr);

    private static native long newThing(long ptr);

    private static native long newArray(long ptr);

    private static native long newArrayOf(long[] ptrs);

    private static native long newObject(long ptr);

    private static native long newObjectOf(long[] ptrs);

    public static ValueMut createNone() {
        return new ValueMut(newNone());
    }

    public static ValueMut createNull() {
        return new ValueMut(newNull());
    }

    public static ValueMut createString(String s) {
        return new ValueMut(newString(s));
    }

    public static ValueMut createBoolean(boolean b) {
        return new ValueMut(newBoolean(b));
    }

    public static ValueMut createDouble(double d) {
        return new ValueMut(newDouble(d));
    }

    public static ValueMut createLong(long l) {
        return new ValueMut(newLong(l));
    }

    public static ValueMut createBigDecimal(BigDecimal d) {
        return new ValueMut(newDecimal(d.toString()));
    }

    public static ValueMut createDuration(Duration d) {
        return new ValueMut(newDuration(d.toMillis()));
    }

    public static ValueMut createDatetime(ZonedDateTime d) {
        return new ValueMut(newDatetime(d.toEpochSecond(), d.getNano()));
    }

    public static ValueMut createUuid(UUID uuid) {
        return new ValueMut(newUuid(uuid.toString()));
    }

    public static ValueMut createId(Id id) {
        return new ValueMut(newId(id.getPtr()));
    }

    public static ValueMut createThing(RecordId recordId) {
        return new ValueMut(newThing(recordId.getPtr()));
    }

    public static ValueMut createArray(Array array) {
        return new ValueMut(newArray(array.getPtr()));
    }

    public static ValueMut createArray(List<ValueMut> values) {
        final long[] ptrs = new long[values.size()];
        int idx = 0;
        // Retrieve the PTR for each element
        for (final ValueMut value : values) {
            ptrs[idx++] = value.getPtr();
        }
        final ValueMut value = new ValueMut(newArrayOf(ptrs));
        // The Values have been moved
        values.forEach(Native::moved);
        return value;
    }

    public static ValueMut createObject(Object object) {
        return new ValueMut(newObject(object.getPtr()));
    }

    public static ValueMut createObject(List<EntryMut> entries) {
        final long[] ptrs = new long[entries.size()];
        int idx = 0;
        // Retrieve the PTR for each element
        for (final EntryMut entry : entries) {
            ptrs[idx++] = entry.getPtr();
        }
        final ValueMut value = new ValueMut(newObjectOf(ptrs));
        // The Entries have been moved
        entries.forEach(Native::moved);
        return value;
    }

    @Override
    final native String toString(long ptr);

    @Override
    final native int hashCode(long ptr);

    @Override
    final native boolean equals(long ptr1, long ptr2);

    @Override
    final native void deleteInstance(long ptr);
}
