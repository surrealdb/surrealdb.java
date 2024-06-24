package com.surrealdb;

class EntryMut extends Native {

    private EntryMut(long ptr) {
        super(ptr);
    }

    private static native long create(String name, long valuePtr);

    static EntryMut newEntry(String name, ValueMut value) {
        final long ptr = create(name, value.getPtr());
        EntryMut entry = new EntryMut(ptr);
        // The lifecycle of ValueMut is now moved to the Entry
        value.moved();
        return entry;
    }

    final protected native boolean deleteInstance(long ptr);

}
