package com.surrealdb;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class ClassValueIterator<T> implements Iterator<T> {

    private final Class<T> clazz;
    private final Iterator<Value> iterator;

    ClassValueIterator(Class<T> clazz, Iterator<Value> iterator) {
        this.clazz = clazz;
        this.iterator = iterator;
    }


    private static java.lang.Object convertArrayValue(Field field, Value value) throws ReflectiveOperationException {
        if (value.isBoolean()) {
            value.getBoolean();
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
        } else if (value.isObject()) {
            final Class<?> subType = getGenericType(field);
            if (subType != null) {
                return convertObject(subType, value.getObject());
            }
        } else if (value.isArray()) {
            final List<java.lang.Object> arrayList = new ArrayList<>();
            for (final Value elementValue : value.getArray()) {
                arrayList.add(convertArrayValue(field, elementValue));
            }
            return arrayList;
        }
        return null;
    }

    private static <T> T convertObject(Class<T> clazz, Object source) throws ReflectiveOperationException {
        final T target = clazz.getConstructor().newInstance();
        for (final Entry entry : source) {
            final Field field = clazz.getDeclaredField(entry.getKey());
            final Value value = entry.getValue();
            field.setAccessible(true);
            if (value.isBoolean()) {
                field.setBoolean(target, value.getBoolean());
            } else if (value.isDouble()) {
                field.setDouble(target, value.getDouble());
            } else if (value.isLong()) {
                field.setLong(target, value.getLong());
            } else if (value.isString()) {
                field.set(target, value.getString());
            } else if (value.isThing()) {
                field.set(target, value.getThing());
            } else if (value.isGeometry()) {
                field.set(target, value.getGeometry());
            } else if (value.isBigdecimal()) {
                field.set(target, value.getBigDecimal());
            } else if (value.isBytes()) {
                field.set(target, value.getBytes());
            } else if (value.isUuid()) {
                field.set(target, value.getUuid());
            } else if (value.isArray()) {
                final List<java.lang.Object> arrayList = new ArrayList<>();
                for (final Value elementValue : value.getArray()) {
                    arrayList.add(convertArrayValue(field, elementValue));
                }
                field.set(target, arrayList);
            } else if (value.isObject()) {
                java.lang.Object o = convertObject(field.getType(), value.getObject());
                field.set(target, o);
            }
        }
        return target;
    }

    private static Class<?> getGenericType(Field field) {
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

    @Override
    final public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    final public T next() {
        final Object source = iterator.next().getObject();
        try {
            return convertObject(clazz, source);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException("Failed to create instance of " + clazz.getName(), e);
        }
    }
}