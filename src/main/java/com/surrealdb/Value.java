package com.surrealdb;

import java.math.BigDecimal;
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

	private native long getDuration(long ptr);

	private native boolean isBytes(long ptr);

	private native byte[] getBytes(long ptr);

	private native boolean isRecordId(long ptr);

	private native long getRecordId(long ptr);

	private native boolean isFile(long ptr);

	private native long getFile(long ptr);

	private native boolean isRange(long ptr);

	private native long getRangeStart(long ptr);

	private native long getRangeEnd(long ptr);

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

	public boolean isDouble() {
		return isDouble(getPtr());
	}

	public double getDouble() {
		return getDouble(getPtr());
	}

	public boolean isLong() {
		return isLong(getPtr());
	}

	public long getLong() {
		return getLong(getPtr());
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
		final long durationMillis = getDuration(getPtr());
		return Duration.ofMillis(durationMillis);
	}

	public <T> T get(Class<T> type) {
		return new ValueClassConverter<>(type).convert(this);
	}
}
