package com.surrealdb;

public class EntryMut extends Native {

	private EntryMut(long ptr) {
		super(ptr);
	}

	private static native long create(String name, long valuePtr);

	public static EntryMut newEntry(String name, ValueMut value) {
		try {
			return new EntryMut(create(name, value.getPtr()));
		} finally {
			// The lifecycle of ValueMut is now moved to the Entry
			value.moved();
		}
	}

	@Override
	final native String toString(long ptr);

	@Override
	final native int hashCode(long ptr);

	@Override
	final native boolean equals(long ptr1, long ptr2);

	private static native void deleteInstance(long ptr);

	@Override
	final NativeCleaner.Deleter deleter() {
		return EntryMut::deleteInstance;
	}

}
