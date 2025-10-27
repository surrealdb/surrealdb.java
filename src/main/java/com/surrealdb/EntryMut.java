package com.surrealdb;

public class EntryMut extends Native {

    private EntryMut(long ptr) {
        super(ptr);
    }

    private static native long create(String name, long valuePtr);

    public static EntryMut newEntry(String name, ValueMut value) {
        final long ptr = create(name, value.getPtr());
        EntryMut entry = new EntryMut(ptr);
        // The lifecycle of ValueMut is now moved to the Entry
        value.moved();
        return entry;
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
