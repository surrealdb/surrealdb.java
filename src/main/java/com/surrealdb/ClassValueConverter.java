package com.surrealdb;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

class ClassValueConverter<T> {

    private final Class<T> clazz;

    ClassValueConverter(Class<T> clazz) {
        this.clazz = clazz;
    }

    private static java.lang.Object convertSingleValue(final Value value) {
        if (value.isBoolean()) {
            return value.getBoolean();
        } else if (value.isDouble()) {
            return value.getDouble();
        } else if (value.isLong()) {
            return value.getLong();
        } else if (value.isString()) {
            return value.getString();
        } else if (value.isThing()) {
            return value.getThing();
        } else if (value.isGeometry()) {
            return value.getGeometry();
        } else if (value.isBigdecimal()) {
            return value.getBigDecimal();
        } else if (value.isBytes()) {
            return value.getBytes();
        } else if (value.isUuid()) {
            return value.getUuid();
        }
        throw new SurrealException("Unsupported value: " + value);
    }


    private static java.lang.Object convertArrayValue(final Field field, final Value value) throws ReflectiveOperationException {
        if (value.isObject()) {
            final Class<?> subType = getGenericType(field);
            if (subType == null) {
                throw new SurrealException("Unsupported field type: " + field);
            }
            return convertObject(subType, value.getObject());
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

    private static <T> T convertObject(Class<T> clazz, Object source) throws ReflectiveOperationException {
        final T target = clazz.getConstructor().newInstance();
        for (final Entry entry : source) {
            final Field field = clazz.getDeclaredField(entry.getKey());
            final Value value = entry.getValue();
            field.setAccessible(true);
            if (value.isArray()) {
                final List<java.lang.Object> arrayList = new ArrayList<>();
                for (final Value elementValue : value.getArray()) {
                    arrayList.add(convertArrayValue(field, elementValue));
                }
                field.set(target, arrayList);
            } else if (value.isObject()) {
                java.lang.Object o = convertObject(field.getType(), value.getObject());
                field.set(target, o);
            } else {
                field.set(target, convertSingleValue(value));
            }
        }
        return target;
    }

    private static Class<?> getGenericType(final Field field) {
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

    final T convertObject(final Value value) {
        try {
            return convertObject(clazz, value.getObject());
        } catch (ReflectiveOperationException e) {
            throw new SurrealException("Failed to create instance of " + clazz.getName(), e);
        }
    }
}