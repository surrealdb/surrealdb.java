package com.surrealdb;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * The Array class represents a native array structure and provides methods to
 * interact with and retrieve values from the array. It implements the Iterable
 * interface for Value type.
 */
public class Array extends Native implements Iterable<Value> {

	Array(long ptr) {
		super(ptr);
	}

	/**
	 * Creates an Array from a heterogeneous sequence of Java values. Supported
	 * element types include {@code null}, {@link String}, the boxed numeric types,
	 * {@link Boolean}, {@link java.math.BigDecimal}, {@link java.util.UUID},
	 * {@link java.time.Duration}, {@link java.time.ZonedDateTime}, and the
	 * SurrealDB wrappers {@link Array}, {@link Id}, {@link RecordId}, and
	 * {@link com.surrealdb.Object}. Unsupported types raise
	 * {@link IllegalArgumentException}.
	 * <p>
	 * To build a single-element {@code [null]} array, cast the argument:
	 * {@code Array.of((java.lang.Object) null)}. A bare {@code Array.of(null)} is a
	 * null varargs array and will throw {@link NullPointerException}.
	 */
	public static Array of(java.lang.Object... elements) {
		Objects.requireNonNull(elements, "elements");
		return fromList(Arrays.asList(elements));
	}

	/**
	 * List-based counterpart to {@link #of(java.lang.Object...)} for callers that
	 * already hold a {@link List}. Named distinctly from {@code of(...)} to avoid a
	 * null-call overload ambiguity.
	 */
	public static Array fromList(List<?> elements) {
		Objects.requireNonNull(elements, "elements");
		final long[] ptrs = new long[elements.size()];
		final ValueMut[] muts = new ValueMut[elements.size()];
		for (int i = 0; i < elements.size(); i++) {
			muts[i] = ValueBoxing.box(elements.get(i));
			ptrs[i] = muts[i].getPtr();
		}
		final Array array = new Array(newOf(ptrs));
		for (final ValueMut m : muts) {
			m.moved();
		}
		return array;
	}

	private static native long newOf(long[] valueMutPtrs);

	private static native long get(long ptr, int idx);

	private static native int len(long ptr);

	private static native long iterator(long ptr);

	private static native long synchronizedIterator(long ptr);

	@Override
	final native String toString(long ptr);

	@Override
	final native int hashCode(long ptr);

	@Override
	final native boolean equals(long ptr1, long ptr2);

	final public Value get(int idx) {
		return new Value(get(getPtr(), idx));
	}

	final public int len() {
		return len(getPtr());
	}

	@Override
	final native void deleteInstance(long ptr);

	@Override
	final public Iterator<Value> iterator() {
		return new ValueIterator(iterator(getPtr()));
	}

	final public <T> Iterator<T> iterator(Class<T> clazz) {
		return new ValueObjectIterator<>(clazz, iterator());
	}

	final public Iterator<Value> synchronizedIterator() {
		return new SynchronizedValueIterator(synchronizedIterator(getPtr()));
	}

	final public <T> Iterator<T> synchronizedIterator(Class<T> clazz) {
		return new ValueObjectIterator<>(clazz, synchronizedIterator());
	}
}
