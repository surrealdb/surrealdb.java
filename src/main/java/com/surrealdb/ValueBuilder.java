package com.surrealdb;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;


class ValueBuilder {

    private static <T> ValueMut convertObject(final T object) throws IllegalAccessException {
        if (object == null) {
            return null;
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
        if (object instanceof ZonedDateTime) {
            return ValueMut.createDatetime((ZonedDateTime) object);
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
        if (object instanceof Optional) {
            final Optional<?> optional = (Optional<?>) object;
            return optional.map(ValueBuilder::convert).orElse(null);
        }
        if (object instanceof Id) {
            return ValueMut.createId((Id) object);
        }
        if (object instanceof RecordId) {
            return ValueMut.createThing((RecordId) object);
        }
        if (object instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) object;
            final List<EntryMut> entries = new ArrayList<>(map.size());
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                final String name = String.valueOf(entry.getKey());
                final ValueMut value = convert(entry.getValue());
                if (value != null) {
                    entries.add(EntryMut.newEntry(name, value));
                }
            }
            return ValueMut.createObject(entries);
        }
        final Field[] fields = object.getClass().getDeclaredFields();
        if (fields.length > 0) {
            final List<EntryMut> entries = new ArrayList<>(fields.length);
            for (final Field field : fields) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                final String name = field.getName();
                final ValueMut value = convert(field.get(object));
                if (value != null) {
                    entries.add(EntryMut.newEntry(name, value));
                }
            }
            return ValueMut.createObject(entries);
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

}
