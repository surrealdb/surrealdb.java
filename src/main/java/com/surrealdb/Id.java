package com.surrealdb;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * The Id class represents a unique identifier that can be either a long value
 * or a string.
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

	/**
	 * Creates an Id whose key is an array of Java values (composite key). See
	 * {@link Array#of(java.lang.Object...)} for the supported element types.
	 */
	public static Id from(java.lang.Object... elements) {
		Objects.requireNonNull(elements, "elements");
		return fromList(Arrays.asList(elements));
	}

	/**
	 * List-based counterpart to {@link #from(java.lang.Object...)}.
	 */
	public static Id from(List<?> elements) {
		Objects.requireNonNull(elements, "elements");
		return fromList(elements);
	}

	private static Id fromList(List<?> elements) {
		final long[] ptrs = new long[elements.size()];
		final ValueMut[] muts = new ValueMut[elements.size()];
		for (int i = 0; i < elements.size(); i++) {
			muts[i] = ValueBoxing.box(elements.get(i));
			ptrs[i] = muts[i].getPtr();
		}
		final Id id = new Id(newArrayId(ptrs));
		for (final ValueMut m : muts) {
			m.moved();
		}
		return id;
	}

	private static native long newLongId(long id);

	private static native long newStringId(String id);

	private static native long newUuidId(String id);

	private static native long newArrayId(long[] valueMutPtrs);

	private static native boolean isLong(long ptr);

	private static native long getLong(long ptr);

	private static native boolean isString(long ptr);

	private static native String getString(long ptr);

	private static native boolean isUuid(long ptr);

	private static native String getUuid(long ptr);

	private static native boolean isArray(long ptr);

	private static native long getArray(long ptr);

	private static native boolean isObject(long ptr);

	private static native long getObject(long ptr);

	@Override
	final native int hashCode(long ptr);

	@Override
	final native String toString(long ptr);

	@Override
	final native boolean equals(long ptr1, long ptr2);

	@Override
	final native void deleteInstance(long ptr);

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
		return new Array(getArray(getPtr()));
	}

	final public boolean isObject() {
		return isObject(getPtr());
	}

	final public Object getObject() {
		return new Object(getObject(getPtr()));
	}

}
