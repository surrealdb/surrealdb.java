package com.surrealdb;

import java.util.ArrayList;
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
	 * <p>
	 * To build a single-element {@code [null]} key, cast the argument:
	 * {@code Id.from((java.lang.Object) null)}. A bare {@code Id.from(null)} is a
	 * null varargs array and will throw {@link NullPointerException}.
	 */
	public static Id from(java.lang.Object... elements) {
		Objects.requireNonNull(elements, "elements");
		return fromList(Arrays.asList(elements));
	}

	/**
	 * List-based counterpart to {@link #from(java.lang.Object...)}. Named
	 * distinctly from {@code from(...)} to avoid a null-call overload ambiguity.
	 */
	public static Id fromList(List<?> elements) {
		Objects.requireNonNull(elements, "elements");
		final List<ValueMut> muts = new ArrayList<>(elements.size());
		for (final java.lang.Object e : elements) {
			muts.add(ValueBoxing.box(e));
		}
		// Box everything first, so a failure leaves nothing half handed over. The
		// native side then takes ownership of the boxed values.
		return new Id(newArrayId(takePtrs(muts)));
	}

	private static native long newLongId(long id);

	private static native long newStringId(String id);

	private static native long newUuidId(String id);

	private static native long newArrayId(long[] valueMutPtrs);

	private native boolean isLong(long ptr);

	private native long getLong(long ptr);

	private native boolean isString(long ptr);

	private native String getString(long ptr);

	private native boolean isUuid(long ptr);

	private native String getUuid(long ptr);

	private native boolean isArray(long ptr);

	private native long getArray(long ptr);

	private native boolean isObject(long ptr);

	private native long getObject(long ptr);

	@Override
	final native int hashCode(long ptr);

	@Override
	final native String toString(long ptr);

	@Override
	final native boolean equals(long ptr1, long ptr2);

	private static native void deleteInstance(long ptr);

	@Override
	final NativeCleaner.Deleter deleter() {
		return Id::deleteInstance;
	}

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
