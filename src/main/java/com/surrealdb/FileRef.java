package com.surrealdb;

/**
 * Reference to a file stored in a SurrealDB bucket. Obtained from
 * {@link Value#getFile()} when {@link Value#isFile()} is true.
 * 
 * Valid only for the lifetime of the {@link Value} it was obtained from; do
 * not retain a reference after that Value is no longer in use.
 */
public class FileRef extends Native {

	FileRef(long ptr) {
		super(ptr);
	}

	private static native String getBucket(long ptr);

	private static native String getKey(long ptr);

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

	@Override
	final native void deleteInstance(long ptr);

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
