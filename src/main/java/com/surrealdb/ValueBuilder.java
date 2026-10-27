package com.surrealdb;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

class ValueBuilder {

	/**
	 * @param topLevel
	 *            true for the object the caller passed in, false for anything
	 *            nested in a Collection, Map, or POJO field. A caller-supplied
	 *            {@code ValueMut} is returned as is at the top level, where the
	 *            call only borrows it. Nested, it is copied, because building the
	 *            enclosing array or object takes ownership of what it is given.
	 */
	private static <T> ValueMut convertObject(final T object, final boolean topLevel) throws IllegalAccessException {
		if (object == null) {
			return ValueMut.createNull();
		}
		if (object instanceof ValueMut) {
			final ValueMut vm = (ValueMut) object;
			return topLevel ? vm : vm.copy();
		}
		if (object instanceof String) {
			return ValueMut.createString((String) object);
		}
		if (object instanceof Double) {
			return ValueMut.createDouble((Double) object);
		}
		if (object instanceof Float) {
			return ValueMut.createDouble((Float) object);
		}
		if (object instanceof Long) {
			return ValueMut.createLong((Long) object);
		}
		if (object instanceof Integer) {
			return ValueMut.createLong((Integer) object);
		}
		if (object instanceof Short) {
			return ValueMut.createLong((Short) object);
		}
		if (object instanceof Boolean) {
			return ValueMut.createBoolean((Boolean) object);
		}
		if (object instanceof BigDecimal) {
			return ValueMut.createBigDecimal((BigDecimal) object);
		}
		if (object instanceof Duration) {
			return ValueMut.createDuration((Duration) object);
		}
		if (object instanceof Instant) {
			return ValueMut.createDatetime((Instant) object);
		}
		if (object instanceof ZonedDateTime) {
			return ValueMut.createDatetime((ZonedDateTime) object);
		}
		if (object instanceof OffsetDateTime) {
			return ValueMut.createDatetime((OffsetDateTime) object);
		}
		if (object instanceof LocalDateTime) {
			return ValueMut.createDatetime((LocalDateTime) object);
		}
		if (object instanceof java.util.Date) {
			return ValueMut.createDatetime((java.util.Date) object);
		}
		if (object instanceof BigInteger) {
			throw new SurrealException("Type not supported: " + object.getClass().getCanonicalName());
		}
		if (object instanceof Collection) {
			final Collection<?> collection = (Collection<?>) object;
			// Create a ValueMut for each element of the collection
			final List<ValueMut> values = new ArrayList<>(collection.size());
			try {
				for (final java.lang.Object element : collection) {
					values.add(convertObject(element, false));
				}
				return ValueMut.createArray(values);
			} catch (Throwable t) {
				// Whatever was not handed over to the array is still ours to free.
				freeAll(values);
				throw t;
			}
		}
		if (object instanceof Map) {
			final Map<?, ?> map = (Map<?, ?>) object;
			final List<EntryMut> entries = new ArrayList<>(map.size());
			try {
				// Each converted value goes straight into its entry, which takes
				// ownership of it before anything else can fail.
				for (final Map.Entry<?, ?> entry : map.entrySet()) {
					final String key = entry.getKey().toString();
					entries.add(EntryMut.newEntry(key, convertObject(entry.getValue(), false)));
				}
				return ValueMut.createObject(entries);
			} catch (Throwable t) {
				freeAll(entries);
				throw t;
			}
		}
		if (object instanceof Optional) {
			final Optional<?> optional = (Optional<?>) object;
			return optional.isPresent() ? convertObject(optional.get(), topLevel) : ValueMut.createNull();
		}
		if (object instanceof Id) {
			return ValueMut.createId((Id) object);
		}
		if (object instanceof UUID) {
			return ValueMut.createUuid((UUID) object);
		}
		if (object instanceof RecordId) {
			return ValueMut.createRecordId((RecordId) object);
		}
		if (object instanceof byte[]) {
			final byte[] bytes = (byte[]) object;
			final List<ValueMut> values = new ArrayList<>(bytes.length);
			for (final byte b : bytes) {
				values.add(ValueMut.createLong(b & 0xFF));
			}
			return ValueMut.createArray(values);
		}
		if (object instanceof Array) {
			return ValueMut.createArray((Array) object);
		}
		if (object instanceof Geometry) {
			return ValueMut.createGeometry((Geometry) object);
		}
		if (object instanceof Object) {
			return ValueMut.createObject((Object) object);
		}
		final Class<?> clazz = object.getClass();
		// Mirror the read path (ValueClassConverter): the cached resolver walks
		// the user-defined hierarchy with the same hiding, naming, and
		// duplicate-rejection semantics so objects round-trip symmetrically.
		final Map<String, Field> fields = SurrealFieldNames.inheritedFieldsBySurrealName(clazz);
		if (fields.isEmpty() && clazz.getDeclaredFields().length == 0) {
			throw new SurrealException("No field found: " + clazz.getCanonicalName());
		}
		final List<EntryMut> entries = new ArrayList<>(fields.size());
		try {
			for (final Map.Entry<String, Field> fieldEntry : fields.entrySet()) {
				final java.lang.Object value = fieldEntry.getValue().get(object);
				if (value != null) {
					entries.add(EntryMut.newEntry(fieldEntry.getKey(), convertObject(value, false)));
				}
			}
			return ValueMut.createObject(entries);
		} catch (Throwable t) {
			freeAll(entries);
			throw t;
		}
	}

	/**
	 * Frees temporaries built here that were not handed over to an enclosing array
	 * or object. Handing over clears the pointer, so what was already taken is a
	 * no-op.
	 */
	private static void freeAll(final List<? extends Native> natives) {
		for (final Native n : natives) {
			n.deleteInstance();
		}
	}

	static <T> ValueMut convert(final T object) {
		try {
			return convertObject(object, true);
		} catch (IllegalAccessException e) {
			throw new SurrealException("Unable to convert object", e);
		}
	}

	static Map<String, ValueMut> convertParams(final Map<String, ?> params) {
		final Map<String, ValueMut> converted = new HashMap<>(params.size() * 2);
		try {
			for (final Map.Entry<String, ?> entry : params.entrySet()) {
				converted.put(entry.getKey(), convert(entry.getValue()));
			}
			return converted;
		} catch (Throwable t) {
			// Free what was converted before the failure; a caller-supplied ValueMut is
			// spared.
			releaseParams(converted, params);
			throw t;
		}
	}

	/**
	 * Frees the temporary {@code ValueMut} that {@link #convert} created from
	 * {@code original}, once the native call that borrowed it has returned. A
	 * {@code ValueMut} supplied by the caller is passed through unchanged by
	 * {@code convert}; it belongs to the caller and is only kept reachable.
	 */
	static void releaseConverted(final ValueMut converted, final java.lang.Object original) {
		if (converted == null) {
			return;
		}
		if (isCallerOwned(converted, original)) {
			Native.reachabilityFence(converted);
		} else {
			converted.deleteInstance();
		}
	}

	/**
	 * {@link #releaseConverted} for each element of {@code converted}, which was
	 * converted from the element at the same index of {@code originals}. Elements
	 * not converted yet (null) are skipped.
	 */
	static void releaseAll(final ValueMut[] converted, final java.lang.Object[] originals) {
		for (int i = 0; i < converted.length; i++) {
			releaseConverted(converted[i], originals[i]);
		}
	}

	/**
	 * {@link #releaseConverted} for each value of {@code converted}, which was
	 * produced by {@link #convertParams} from {@code originals}.
	 */
	static void releaseParams(final Map<String, ValueMut> converted, final Map<String, ?> originals) {
		for (final Map.Entry<String, ValueMut> entry : converted.entrySet()) {
			releaseConverted(entry.getValue(), originals.get(entry.getKey()));
		}
	}

	// convert() hands back the caller's own ValueMut, possibly inside Optionals.
	private static boolean isCallerOwned(final ValueMut converted, final java.lang.Object original) {
		java.lang.Object o = original;
		while (o instanceof Optional) {
			o = ((Optional<?>) o).orElse(null);
		}
		return converted == o;
	}

}
