package com.surrealdb;

/**
 * Reference to a file stored in a SurrealDB bucket. Obtained from
 * {@link Value#getFile()} when {@link Value#isFile()} is true.
 *
 * Holds its own reference to the file data, so it stays valid independently of
 * the {@link Value} it was obtained from.
 */
public class FileRef extends Native {

	FileRef(long ptr) {
		super(ptr);
	}

	private native String getBucket(long ptr);

	private native String getKey(long ptr);

	@Override
	final native String toString(long ptr);

	@Override
	final int hashCode(long ptr) {
		return (getBucket(ptr) + ":" + getKey(ptr)).hashCode();
	}

	@Override
	final boolean equals(long ptr1, long ptr2) {
		return getBucket(ptr1).equals(getBucket(ptr2)) && getKey(ptr1).equals(getKey(ptr2));
	}

	private static native void deleteInstance(long ptr);

	@Override
	final NativeCleaner.Deleter deleter() {
		return FileRef::deleteInstance;
	}

	/**
	 * Returns the bucket name.
	 */
	public String getBucket() {
		return getBucket(getPtr());
	}

	/**
	 * Returns the file key (path within the bucket). Always starts with "/".
	 */
	public String getKey() {
		return getKey(getPtr());
	}
}
