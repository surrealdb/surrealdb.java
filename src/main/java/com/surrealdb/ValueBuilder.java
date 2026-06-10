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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

class ValueBuilder {

	private static <T> ValueMut convertObject(final T object) throws IllegalAccessException {
		if (object == null) {
			return ValueMut.createNull();
		}
		if (object instanceof ValueMut) {
			return (ValueMut) object;
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
			final List<ValueMut> values = collection.stream().map(ValueBuilder::convert).collect(Collectors.toList());
			return ValueMut.createArray(values);
		}
		if (object instanceof Map) {
			final Map<?, ?> map = (Map<?, ?>) object;
			final List<EntryMut> entries = new ArrayList<>(map.size());
			// Create a ValueMut for each value of the map
			for (final Map.Entry<?, ?> entry : map.entrySet()) {
				final String key = entry.getKey().toString();
				final ValueMut value = convert(entry.getValue());
				entries.add(EntryMut.newEntry(key, value));
			}
			return ValueMut.createObject(entries);
		}
		if (object instanceof Optional) {
			final Optional<?> optional = (Optional<?>) object;
			return optional.map(ValueBuilder::convert).orElseGet(ValueMut::createNull);
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
		if (object instanceof Object) {
			return ValueMut.createObject((Object) object);
		}
		final Class<?> clazz = object.getClass();
		final Field[] declaredFields = clazz.getDeclaredFields();
		if (!SurrealFieldNames.hasUserSuperclass(clazz) && !SurrealFieldNames.hasDeclaredSurrealName(declaredFields)) {
			// Fast path: a single user-defined class with raw Java names. Java
			// forbids duplicate field names within one class, so there is
			// nothing to validate.
			if (declaredFields.length > 0) {
				final List<EntryMut> entries = new ArrayList<>(declaredFields.length);
				for (final Field field : declaredFields) {
					if (!SurrealFieldNames.isSerializableField(field)) {
						continue;
					}
					field.setAccessible(true);
					final java.lang.Object value = field.get(object);
					if (value != null) {
						entries.add(EntryMut.newEntry(field.getName(), convert(value)));
					}
				}
				return ValueMut.createObject(entries);
			}
		} else {
			// Mirror the read path (ValueClassConverter): walk the user-defined
			// hierarchy with the same hiding, naming, and duplicate-rejection
			// semantics so objects round-trip symmetrically.
			final Map<String, Field> fields = SurrealFieldNames.inheritedFieldsBySurrealName(clazz);
			if (!fields.isEmpty() || declaredFields.length > 0) {
				final List<EntryMut> entries = new ArrayList<>(fields.size());
				for (final Map.Entry<String, Field> fieldEntry : fields.entrySet()) {
					final Field field = fieldEntry.getValue();
					field.setAccessible(true);
					final java.lang.Object value = field.get(object);
					if (value != null) {
						entries.add(EntryMut.newEntry(fieldEntry.getKey(), convert(value)));
					}
				}
				return ValueMut.createObject(entries);
			}
		}
		throw new SurrealException("No field found: " + object.getClass().getCanonicalName());
	}

	static <T> ValueMut convert(final T object) {
		try {
			return convertObject(object);
		} catch (IllegalAccessException e) {
			throw new SurrealException("Unable to convert object", e);
		}
	}

	static Map<String, ValueMut> convertParams(final Map<String, ?> params) {
		return params.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, entry -> convert(entry.getValue())));
	}

}
