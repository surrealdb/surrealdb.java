package com.surrealdb;

import java.util.List;

class ValueMut extends Native {

    private ValueMut(long ptr) {
        super(ptr);
    }

    private static native long newString(String s);

    private static native long newBoolean(boolean b);

    private static native long newDouble(double d);

    private static native long newLong(long l);

    private static native long newArray(long[] ptrs);

    private static native long newObject(long[] ptrs);

    static ValueMut createString(String s) {
        return new ValueMut(newString(s));
    }

    static ValueMut createBoolean(boolean b) {
        return new ValueMut(newBoolean(b));
    }

    static ValueMut createDouble(double d) {
        return new ValueMut(newDouble(d));
    }

    static ValueMut createLong(long l) {
        return new ValueMut(newLong(l));
    }

    static ValueMut createArray(List<ValueMut> values) {
        final long[] ptrs = new long[values.size()];
        int idx = 0;
        // Retrieve the PTR for each element
        for (final ValueMut value : values) {
            ptrs[idx++] = value.getPtr();
        }
        final ValueMut value = new ValueMut(newArray(ptrs));
        // The Values have been moved
        values.forEach(Native::moved);
        return value;
    }

    static ValueMut createObject(List<EntryMut> entries) {
        final long[] ptrs = new long[entries.size()];
        int idx = 0;
        // Retrieve the PTR for each element
        for (final EntryMut entry : entries) {
            ptrs[idx++] = entry.getPtr();
        }
        final ValueMut value = new ValueMut(newObject(ptrs));
        // The Entries have been moved
        entries.forEach(Native::moved);
        return value;
    }

    @Override
    final protected native String toString(long ptr);

    @Override
    final protected native int hashCode(long ptr);

    @Override
    final protected native boolean equals(long ptr1, long ptr2);

    @Override
    final protected native boolean deleteInstance(long ptr);
}
