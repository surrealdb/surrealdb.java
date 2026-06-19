package com.surrealdb;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Boxes raw Java values into {@link ValueMut} instances, used by factories like
 * {@link Array#of(java.lang.Object...)} and
 * {@link Id#from(java.lang.Object...)} to accept heterogeneous element lists.
 */
final class ValueBoxing {

	private ValueBoxing() {
	}

	static ValueMut box(java.lang.Object e) {
		if (e == null) {
			return ValueMut.createNull();
		}
		if (e instanceof String) {
			return ValueMut.createString((String) e);
		}
		if (e instanceof Long) {
			return ValueMut.createLong((Long) e);
		}
		if (e instanceof Integer) {
			return ValueMut.createLong(((Integer) e).longValue());
		}
		if (e instanceof Short) {
			return ValueMut.createLong(((Short) e).longValue());
		}
		if (e instanceof Byte) {
			return ValueMut.createLong(((Byte) e).longValue());
		}
		if (e instanceof Boolean) {
			return ValueMut.createBoolean((Boolean) e);
		}
		if (e instanceof Double) {
			return ValueMut.createDouble((Double) e);
		}
		if (e instanceof Float) {
			return ValueMut.createDouble(((Float) e).doubleValue());
		}
		if (e instanceof BigDecimal) {
			return ValueMut.createBigDecimal((BigDecimal) e);
		}
		if (e instanceof UUID) {
			return ValueMut.createUuid((UUID) e);
		}
		if (e instanceof Duration) {
			return ValueMut.createDuration((Duration) e);
		}
		if (e instanceof ZonedDateTime) {
			return ValueMut.createDatetime((ZonedDateTime) e);
		}
		if (e instanceof Array) {
			return ValueMut.createArray((Array) e);
		}
		if (e instanceof Id) {
			return ValueMut.createId((Id) e);
		}
		if (e instanceof RecordId) {
			return ValueMut.createRecordId((RecordId) e);
		}
		if (e instanceof com.surrealdb.Object) {
			return ValueMut.createObject((com.surrealdb.Object) e);
		}
		if (e instanceof Geometry) {
			return ValueMut.createGeometry((Geometry) e);
		}
		throw new IllegalArgumentException("unsupported element type " + e.getClass().getName());
	}
}
