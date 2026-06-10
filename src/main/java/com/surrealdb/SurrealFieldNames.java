package com.surrealdb;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

	static boolean hasDeclaredSurrealName(final Field[] fields) {
		for (final Field field : fields) {
			if (isSerializableField(field) && field.getAnnotation(SurrealName.class) != null) {
				return true;
			}
		}
		return false;
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
		final Set<String> seenJavaNames = new HashSet<>();
		Class<?> c = clazz;
		while (c != null && c != java.lang.Object.class) {
			for (final Field field : c.getDeclaredFields()) {
				if (!isSerializableField(field)) {
					continue;
				}
				// Java field hiding is based on the declared Java name, not the
				// resolved SurrealDB name. Keep hidden superclass fields out even
				// when the subclass field is annotated to a different key.
				if (!seenJavaNames.add(field.getName())) {
					continue;
				}
				final String name = nameFor(field);
				if (fields.containsKey(name)) {
					throw duplicateName(name, fields.get(name), field);
				}
				fields.put(name, field);
			}
			c = c.getSuperclass();
		}
		return fields;
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
