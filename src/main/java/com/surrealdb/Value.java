package com.surrealdb;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * The Value class represents a general-purpose wrapper for different types of
 * values. It provides various methods to interact with and retrieve values such
 * as arrays, objects, strings, numbers, and more.
 */
public class Value extends Native implements AutoCloseable {

	Value(long ptr) {
		super(ptr);
	}

	private native boolean isNone(long ptr);

	private native boolean isNull(long ptr);

	private native boolean isBoolean(long ptr);

	private native boolean getBoolean(long ptr);

	private native boolean isDouble(long ptr);

	private native double getDouble(long ptr);

	private native boolean isLong(long ptr);

	private native long getLong(long ptr);

	private native boolean isBigInteger(long ptr);

	private native String getBigInteger(long ptr);

	private native boolean isNumber(long ptr);

	private native String getNumberKind(long ptr);

	private native boolean isBigDecimal(long ptr);

	private native String getBigDecimal(long ptr);

	private native boolean isString(long ptr);

	private native String getString(long ptr);

	private native boolean isUuid(long ptr);

	private native String getUuid(long ptr);

	private native boolean isArray(long ptr);

	private native long getArray(long ptr);

	private native boolean isObject(long ptr);

	private native long getObject(long ptr);

	private native boolean isGeometry(long ptr);

	private native long getGeometry(long ptr);

	private native boolean isDateTime(long ptr);

	private native long[] getDateTime(long ptr);

	private native boolean isDuration(long ptr);

	private native long[] getDuration(long ptr);

	private native boolean isBytes(long ptr);

	private native byte[] getBytes(long ptr);

	private native boolean isRecordId(long ptr);

	private native long getRecordId(long ptr);

	private native boolean isFile(long ptr);

	private native long getFile(long ptr);

	private native boolean isRange(long ptr);

	private native long getRangeStart(long ptr);

	private native long getRangeEnd(long ptr);

	private native boolean isRangeStartInclusive(long ptr);

	private native boolean isRangeEndInclusive(long ptr);

	private native boolean isSet(long ptr);

	private native long getSet(long ptr);

	private native boolean isTable(long ptr);

	private native String getTable(long ptr);

	@Override
	final native String toString(long ptr);

	@Override
	final native int hashCode(long ptr);

	@Override
	final native boolean equals(long ptr1, long ptr2);

	private static native void deleteInstance(long ptr);

	@Override
	final NativeCleaner.Deleter deleter() {
		return Value::deleteInstance;
	}

	public boolean isArray() {
		return isArray(getPtr());
	}

	public Array getArray() {
		return new Array(getArray(getPtr()));
	}

	public boolean isObject() {
		return isObject(getPtr());
	}

	public Object getObject() {
		return new Object(getObject(getPtr()));
	}

	public boolean isBoolean() {
		return isBoolean(getPtr());
	}

	public boolean getBoolean() {
		return getBoolean(getPtr());
	}

	/**
	 * True for every floating-point number: <code>float</code> (f64) and, with
	 * SurrealDB 4.0, <code>f32</code> and <code>f16</code>.
	 */
	public boolean isDouble() {
		return isDouble(getPtr());
	}

	/**
	 * The value of a floating-point number as a <code>double</code>. An
	 * <code>f32</code> or <code>f16</code> widens exactly.
	 */
	public double getDouble() {
		return getDouble(getPtr());
	}

	/**
	 * True for every integer number whose value fits a Java <code>long</code>:
	 * <code>int</code> (i64) and, with SurrealDB 4.0, <code>i8</code>,
	 * <code>u8</code>, <code>i16</code>, <code>u16</code>, <code>i32</code>,
	 * <code>u32</code>, and <code>u64</code> up to {@link Long#MAX_VALUE}.
	 * <p>
	 * A <code>u64</code> above {@link Long#MAX_VALUE} is never reported as a long,
	 * it would wrap to a negative number. Check {@link #isBigInteger()} for it.
	 */
	public boolean isLong() {
		return isLong(getPtr());
	}

	/**
	 * The value of an integer number as a <code>long</code>.
	 *
	 * @throws SurrealException
	 *             if the value is a <code>u64</code> above {@link Long#MAX_VALUE};
	 *             use {@link #getBigInteger()} instead.
	 */
	public long getLong() {
		return getLong(getPtr());
	}

	/**
	 * True for an integer number that does not fit a Java <code>long</code>, which
	 * can only be a <code>u64</code> above {@link Long#MAX_VALUE}. Exactly one of
	 * {@link #isLong()} and {@code isBigInteger()} is true for an integer number.
	 */
	public boolean isBigInteger() {
		return isBigInteger(getPtr());
	}

	/**
	 * The exact value of any integer number as a {@link BigInteger}, whatever its
	 * width, including a <code>u64</code> above {@link Long#MAX_VALUE}.
	 */
	public BigInteger getBigInteger() {
		return new BigInteger(getBigInteger(getPtr()));
	}

	/**
	 * True for any number, whatever its kind.
	 */
	public boolean isNumber() {
		return isNumber(getPtr());
	}

	/**
	 * The exact kind of a number, such as {@link NumberType#I8} or
	 * {@link NumberType#FLOAT}. A 3.x server only produces {@link NumberType#INT},
	 * {@link NumberType#FLOAT} and {@link NumberType#DECIMAL}.
	 * <p>
	 * Results keep their kind on the embedded engine and on a remote SurrealDB 4.0
	 * server. Values you send are another matter: over a remote connection a sized
	 * value bound as a variable is widened (sized integers become
	 * {@link NumberType#INT}, a <code>u64</code> above {@link Long#MAX_VALUE}
	 * becomes {@link NumberType#DECIMAL}, and <code>f16</code>/<code>f32</code>
	 * become {@link NumberType#FLOAT}), unless a <code>TYPE</code> schema field
	 * converts it back.
	 *
	 * @throws NullPointerException
	 *             if the value is not a number, see {@link #isNumber()}.
	 */
	public NumberType getNumberType() {
		return NumberType.fromSurrealName(getNumberKind(getPtr()));
	}

	public boolean isBigDecimal() {
		return isBigDecimal(getPtr());
	}

	public BigDecimal getBigDecimal() {
		return new BigDecimal(getBigDecimal(getPtr()));
	}

	public boolean isNull() {
		return isNull(getPtr());
	}

	public boolean isNone() {
		return isNone(getPtr());
	}

	public boolean isString() {
		return isString(getPtr());
	}

	public String getString() {
		return getString(getPtr());
	}

	public boolean isUuid() {
		return isUuid(getPtr());
	}

	public UUID getUuid() {
		return UUID.fromString(getUuid(getPtr()));
	}

	public boolean isRecordId() {
		return isRecordId(getPtr());
	}

	public RecordId getRecordId() {
		return new RecordId(getRecordId(getPtr()));
	}

	public boolean isFile() {
		return isFile(getPtr());
	}

	public FileRef getFile() {
		return new FileRef(getFile(getPtr()));
	}

	public boolean isRange() {
		return isRange(getPtr());
	}

	/**
	 * Start bound of the range (included or excluded), or empty if unbounded.
	 */
	public Optional<Value> getRangeStart() {
		final long p = getRangeStart(getPtr());
		return p == 0 ? Optional.empty() : Optional.of(new Value(p));
	}

	/**
	 * End bound of the range (included or excluded), or empty if unbounded.
	 */
	public Optional<Value> getRangeEnd() {
		final long p = getRangeEnd(getPtr());
		return p == 0 ? Optional.empty() : Optional.of(new Value(p));
	}

	/**
	 * Whether the start bound of the range is inclusive ({@code a..} rather than
	 * {@code a>..}). Also false when the range has no start bound.
	 */
	public boolean isRangeStartInclusive() {
		return isRangeStartInclusive(getPtr());
	}

	/**
	 * Whether the end bound of the range is inclusive ({@code ..=b} rather than
	 * {@code ..b}). Also false when the range has no end bound.
	 */
	public boolean isRangeEndInclusive() {
		return isRangeEndInclusive(getPtr());
	}

	/**
	 * Whether this value is a set: a sorted collection without duplicates. Sets are
	 * distinct from arrays, so {@link #isArray()} is false for them.
	 */
	public boolean isSet() {
		return isSet(getPtr());
	}

	/**
	 * Returns the elements of this set, in the set's (sorted) order.
	 */
	public Array getSet() {
		return new Array(getSet(getPtr()));
	}

	public boolean isTable() {
		return isTable(getPtr());
	}

	/**
	 * Table name when this value is a table type.
	 */
	public String getTable() {
		return getTable(getPtr());
	}

	public boolean isBytes() {
		return isBytes(getPtr());
	}

	public byte[] getBytes() {
		return getBytes(getPtr());
	}

	public boolean isGeometry() {
		return isGeometry(getPtr());
	}

	public Geometry getGeometry() {
		return new Geometry(getGeometry(getPtr()));
	}

	public boolean isDateTime() {
		return isDateTime(getPtr());
	}

	public ZonedDateTime getDateTime() {
		final long[] timestamps = getDateTime(getPtr());
		final Instant instant = Instant.ofEpochSecond(timestamps[0], timestamps[1]);
		return ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));
	}

	public boolean isDuration() {
		return isDuration(getPtr());
	}

	public Duration getDuration() {
		final long[] duration = getDuration(getPtr());
		return Duration.ofSeconds(duration[0], duration[1]);
	}

	public <T> T get(Class<T> type) {
		return new ValueClassConverter<>(type).convert(this);
	}

	/**
	 * {@link #get(Class)}, then {@link #close()}. For values that only the SDK
	 * holds, such as a result it converts before returning it.
	 */
	final <T> T getAndClose(Class<T> type) {
		try {
			return get(type);
		} finally {
			close();
		}
	}
}
