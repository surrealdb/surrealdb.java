package com.surrealdb;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

class ValueClassConverter<T> {

    private final Class<T> clazz;

    ValueClassConverter(Class<T> clazz) {
        this.clazz = clazz;
    }

    private static java.lang.Object convertSingleValue(final Value value) {
        if (value.isBoolean())
            return value.getBoolean();
        if (value.isDouble())
            return value.getDouble();
        if (value.isLong())
            return value.getLong();
        if (value.isString())
            return value.getString();
        if (value.isThing())
            return value.getThing();
        if (value.isGeometry())
            return value.getGeometry();
        if (value.isBigdecimal())
            return value.getBigDecimal();
        if (value.isBytes())
            return value.getBytes();
        if (value.isUuid())
            return value.getUuid();
        if (value.isDuration())
            return value.getDuration();
        if (value.isDateTime())
            return value.getDateTime();
        throw new SurrealException("Unsupported value: " + value);
    }

    private static <T> void setSingleValue(final Field field, final Class<?> type, final T target, final Value value) throws IllegalAccessException {
        if (value.isBoolean()) {
            field.setBoolean(target, value.getBoolean());
        } else if (value.isDouble()) {
            final double d = value.getDouble();
            if (type == Double.TYPE)
                field.setDouble(target, d);
            else if (type == Float.TYPE)
                field.setFloat(target, (float) d);
            else if (type == Float.class)
                field.set(target, (float) d);
            else field.set(target, d);
        } else if (value.isLong()) {
            final long l = value.getLong();
            if (type == Long.TYPE)
                field.setLong(target, l);
            else if (type == Integer.TYPE)
                field.setInt(target, (int) l);
            else if (type == Integer.class)
                field.set(target, (int) l);
            else if (type == Short.TYPE)
                field.setShort(target, (short) l);
            else if (type == Short.class)
                field.set(target, (short) l);
            else
                field.set(target, l);
        } else if (value.isString()) {
            field.set(target, value.getString());
        } else if (value.isThing()) {
            if (field.getType() == Id.class) {
                field.set(target, value.getThing().getId());
            } else {
                field.set(target, value.getThing());
            }
        } else if (value.isGeometry()) {
            field.set(target, value.getGeometry());
        } else if (value.isBigdecimal()) {
            field.set(target, value.getBigDecimal());
        } else if (value.isBytes()) {
            field.set(target, value.getBytes());
        } else if (value.isUuid()) {
            field.set(target, value.getUuid());
        } else if (value.isDuration()) {
            field.set(target, value.getDuration());
        } else if (value.isDateTime()) {
            field.set(target, value.getDateTime());
        } else {
            throw new SurrealException("Unsupported value: " + value);
        }
    }

    private static java.lang.Object convertArrayValue(final Field field, final Value value) throws ReflectiveOperationException {
        if (value.isObject()) {
            final Class<?> subType = getGenericType(field);
            if (subType == null) {
                throw new SurrealException("Unsupported field type: " + field);
            }
            return convert(subType, value.getObject());
        } else if (value.isArray()) {
            final List<java.lang.Object> arrayList = new ArrayList<>();
            for (final Value elementValue : value.getArray()) {
                arrayList.add(convertArrayValue(field, elementValue));
            }
            return arrayList;
        } else {
            return convertSingleValue(value);
        }
    }

    private static <T> T convert(Class<T> clazz, Object source) throws ReflectiveOperationException {
        final T target = clazz.getConstructor().newInstance();
        for (final Entry entry : source) {
            try {
                final String key = entry.getKey();
                final Field field = clazz.getField(key);
                final Value value = entry.getValue();
                field.setAccessible(true);
                final Class<?> type = field.getType();
                if (value.isArray()) {
                    final List<java.lang.Object> arrayList = new ArrayList<>();
                    for (final Value elementValue : value.getArray()) {
                        arrayList.add(convertArrayValue(field, elementValue));
                    }
                    setFieldObject(field, type, target, arrayList);
                } else if (value.isObject()) {
                    java.lang.Object o = convert(type, value.getObject());
                    setFieldObject(field, type, target, o);
                } else {
                    setFieldSingleValue(field, type, target, value);
                }
            } catch (NoSuchFieldException e) {
                // Safe to ignore
            }
        }
        return target;
    }

    private static <T, V> void setFieldObject(Field field, Class<?> type, T target, V value) throws ReflectiveOperationException {
        if (Optional.class.equals(type)) {
            field.set(target, Optional.of(value));
        } else {
            field.set(target, value);
        }
    }

    private static <T, V> void setFieldSingleValue(Field field, Class<?> type, T target, Value value) throws ReflectiveOperationException {
        if (Optional.class.equals(type)) {
            field.set(target, convertSingleValue(value));
        } else {
            setSingleValue(field, type, target, value);
        }
    }

    static Class<?> getGenericType(final Field field) {
        // Check if the field is parameterized
        if (field.getGenericType() instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) field.getGenericType();

            // Get the actual type arguments (generics)
            final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

            if (actualTypeArguments.length > 0) {
                // Return the first type argument
                return (Class<?>) actualTypeArguments[0];
            }
        }
        return null;
    }

    final T convert(final Value value) {
        try {
            if (value.isNone() || value.isNull()) {
                return null;
            }

            if (!value.isObject()) {
                throw new SurrealException("Unexpected value: " + value);
            }

            return convert(clazz, value.getObject());
        } catch (ReflectiveOperationException e) {
            throw new SurrealException("Failed to create instance of " + clazz.getName(), e);
        }
    }
}
