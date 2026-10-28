package com.surrealdb;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collection;
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

	private static native long newDuration(long seconds, int nanos);

	private static native long newDatetime(long seconds, long nanos);

	private static native long newUuid(String s);

	private static native long newId(long ptr);

	private static native long newRecordId(long ptr);

	private static native long newGeometry(long ptr);

	private static native long newArray(long ptr);

	private static native long newArrayOf(long[] ptrs);

	private static native long newSetOf(long[] ptrs);

	private static native long newObject(long ptr);

	private static native long newObjectOf(long[] ptrs);

	private static native long newFile(String bucket, String key);

	private static native long newTable(String name);

	private native long copy(long ptr);

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

	/**
	 * Creates a duration value with full nanosecond precision.
	 *
	 * @param d
	 *            the duration; SurrealDB durations are unsigned
	 * @return a new duration value
	 * @throws SurrealException
	 *             if {@code d} is negative
	 */
	public static ValueMut createDuration(Duration d) {
		return new ValueMut(newDuration(d.getSeconds(), d.getNano()));
	}

	public static ValueMut createDatetime(Instant i) {
		return new ValueMut(newDatetime(i.getEpochSecond(), i.getNano()));
	}

	public static ValueMut createDatetime(ZonedDateTime d) {
		return createDatetime(d.toInstant());
	}

	public static ValueMut createDatetime(OffsetDateTime d) {
		return createDatetime(d.toInstant());
	}

	/**
	 * Creates a SurrealDB datetime from a local date-time by interpreting it as
	 * UTC.
	 * <p>
	 * Prefer {@link Instant}, {@link OffsetDateTime}, or {@link ZonedDateTime} when
	 * the source value represents an absolute point in time or carries zone/offset
	 * information.
	 */
	public static ValueMut createDatetime(LocalDateTime d) {
		return createDatetime(d.toInstant(ZoneOffset.UTC));
	}

	/**
	 * Creates a SurrealDB datetime from a {@link java.util.Date}.
	 * <p>
	 * {@code java.sql.Timestamp} keeps its nanosecond precision.
	 * {@code java.sql.Date} and {@code java.sql.Time} do not support
	 * {@link java.util.Date#toInstant()}; they are converted through their
	 * epoch-millisecond value instead.
	 */
	public static ValueMut createDatetime(java.util.Date d) {
		try {
			return createDatetime(d.toInstant());
		} catch (UnsupportedOperationException e) {
			// java.sql.Date and java.sql.Time refuse toInstant()
			return createDatetime(Instant.ofEpochMilli(d.getTime()));
		}
	}

	public static ValueMut createUuid(UUID uuid) {
		return new ValueMut(newUuid(uuid.toString()));
	}

	public static ValueMut createId(Id id) {
		try {
			return new ValueMut(newId(id.getPtr()));
		} finally {
			reachabilityFence(id);
		}
	}

	public static ValueMut createRecordId(RecordId recordId) {
		try {
			return new ValueMut(newRecordId(recordId.getPtr()));
		} finally {
			reachabilityFence(recordId);
		}
	}

	public static ValueMut createGeometry(Geometry geometry) {
		try {
			return new ValueMut(newGeometry(geometry.getPtr()));
		} finally {
			reachabilityFence(geometry);
		}
	}

	public static ValueMut createArray(Array array) {
		try {
			return new ValueMut(newArray(array.getPtr()));
		} finally {
			reachabilityFence(array);
		}
	}

	public static ValueMut createArray(List<ValueMut> values) {
		// The native side takes ownership of the values.
		return new ValueMut(newArrayOf(takePtrs(values)));
	}

	/**
	 * Creates a set value: a sorted collection without duplicates, SurrealQL's
	 * {@code set} type. Elements are converted like query bindings, so a
	 * {@code ValueMut} element is copied and stays usable by the caller.
	 * <p>
	 * A {@link java.util.Set} passed as a binding or POJO field is still sent as an
	 * array, because a set does not coerce into an {@code array} field. Use this
	 * method where a SurrealQL set is wanted.
	 *
	 * @param elements
	 *            the elements of the set
	 * @return the set value
	 */
	public static ValueMut createSet(Collection<?> elements) {
		// A caller's ValueMut element is copied, not consumed.
		final List<ValueMut> values = ValueBuilder.convertElements(elements);
		try {
			// The native side takes ownership of the values.
			return new ValueMut(newSetOf(takePtrs(values)));
		} catch (Throwable t) {
			// Whatever was not handed over is still ours to free.
			ValueBuilder.freeAll(values);
			throw t;
		}
	}

	public static ValueMut createObject(Object object) {
		try {
			return new ValueMut(newObject(object.getPtr()));
		} finally {
			reachabilityFence(object);
		}
	}

	public static ValueMut createObject(List<EntryMut> entries) {
		// The native side takes ownership of the entries.
		return new ValueMut(newObjectOf(takePtrs(entries)));
	}

	public static ValueMut createFile(String bucket, String key) {
		return new ValueMut(newFile(bucket, key));
	}

	public static ValueMut createTable(String name) {
		return new ValueMut(newTable(name));
	}

	/**
	 * Returns an independent copy of this value, owned by the caller. Unlike
	 * {@link #createArray(List)} and {@code EntryMut.newEntry}, this value is left
	 * valid and reusable.
	 */
	ValueMut copy() {
		return new ValueMut(copy(getPtr()));
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
		return ValueMut::deleteInstance;
	}
}
