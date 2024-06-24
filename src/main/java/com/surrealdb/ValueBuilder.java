package com.surrealdb;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


class ValueBuilder extends Native {

    ValueBuilder(long ptr) {
        super(ptr);
    }

    private static <T> ValueMut convertObject(final T object) throws IllegalAccessException {
        if (object == null) {
            return null;
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
        final Field[] fields = object.getClass().getFields();
        if (fields.length > 0) {
            final List<EntryMut> entries = new ArrayList<>(fields.length);
            for (Field field : fields) {
                final String name = field.getName();
                final ValueMut value = convert(field.get(object));
                if (value != null) {
                    entries.add(EntryMut.newEntry(name, value));
                }
            }
            return ValueMut.createObject(entries);
        }
        throw new SurrealException("Type not supported: " + object.getClass().getName());
    }

    static <T> ValueMut convert(final T object) {
        try {
            return convertObject(object);
        } catch (IllegalAccessException e) {
            throw new SurrealException("Unable to convert object", e);
        }
    }

    @Override
    final protected native String toString(long ptr);

    @Override
    final protected native int hashCode(long ptr);

    @Override
    final protected native boolean equals(long ptr1, long ptr2);

    @Override
    final protected native boolean deleteInstance(long ptr);

}
