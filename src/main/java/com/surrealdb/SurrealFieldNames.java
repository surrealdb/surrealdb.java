package com.surrealdb;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

final class SurrealFieldNames {

	private SurrealFieldNames() {
	}

	static boolean isSerializableField(final Field field) {
		final int mods = field.getModifiers();
		return !Modifier.isStatic(mods) && !Modifier.isTransient(mods);
	}

	static String nameFor(final Field field) {
		final SurrealName name = field.getAnnotation(SurrealName.class);
		if (name == null) {
			return field.getName();
		}
		final String value = name.value();
		if (value == null || value.trim().isEmpty()) {
			throw new SurrealException("@SurrealName value must not be blank on " + describe(field));
		}
		return value;
	}

	static void ensureUniqueDeclaredNames(final Class<?> clazz) {
		final Map<String, Field> fields = new HashMap<>();
		for (final Field field : clazz.getDeclaredFields()) {
			if (!isSerializableField(field)) {
				continue;
			}
			putUnique(fields, nameFor(field), field);
		}
	}

	static Map<String, Field> inheritedFieldsBySurrealName(final Class<?> clazz) {
		final Map<String, Field> fields = new HashMap<>();
		Class<?> c = clazz;
		while (c != null && c != java.lang.Object.class) {
			for (final Field field : c.getDeclaredFields()) {
				if (!isSerializableField(field)) {
					continue;
				}
				final String name = nameFor(field);
				// Preserve existing field-hiding behavior for unannotated raw names:
				// the concrete class wins over a same-named superclass field.
				if (fields.containsKey(name) && !isPlainRawName(field, name)) {
					throw duplicateName(name, fields.get(name), field);
				}
				if (!fields.containsKey(name)) {
					fields.put(name, field);
				}
			}
			c = c.getSuperclass();
		}
		return fields;
	}

	private static boolean isPlainRawName(final Field field, final String name) {
		return field.getAnnotation(SurrealName.class) == null && field.getName().equals(name);
	}

	private static void putUnique(final Map<String, Field> fields, final String name, final Field field) {
		final Field previous = fields.put(name, field);
		if (previous != null) {
			throw duplicateName(name, previous, field);
		}
	}

	private static SurrealException duplicateName(final String name, final Field first, final Field second) {
		return new SurrealException(
				"Duplicate SurrealDB field name '" + name + "' on " + describe(first) + " and " + describe(second));
	}

	private static String describe(final Field field) {
		return field.getDeclaringClass().getName() + "." + field.getName();
	}
}
