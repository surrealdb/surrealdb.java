package com.surrealdb;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.LinkedHashMap;
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

	static boolean hasUserSuperclass(final Class<?> clazz) {
		final Class<?> superclass = clazz.getSuperclass();
		return superclass != null && !isJdkType(superclass);
	}

	/**
	 * Maps resolved SurrealDB keys to their fields, walking the user-defined part
	 * of the class hierarchy (subclass first, in declaration order). The walk stops
	 * at the first JDK class so JDK internals (e.g. {@code java.lang.Enum.name},
	 * {@code Throwable.detailMessage}) are never serialized or reflectively
	 * assigned.
	 */
	static Map<String, Field> inheritedFieldsBySurrealName(final Class<?> clazz) {
		final Map<String, Field> fields = new LinkedHashMap<>();
		final Set<String> seenJavaNames = new HashSet<>();
		Class<?> c = clazz;
		while (c != null && !isJdkType(c)) {
			for (final Field field : c.getDeclaredFields()) {
				// Java field hiding is based on the declared Java name, not the
				// resolved SurrealDB name, and applies to every declared field
				// (JLS 8.3) — including static and transient ones. Record the
				// name before the serializability check so a non-serializable
				// hider still keeps its hidden superclass field out.
				if (!seenJavaNames.add(field.getName())) {
					continue;
				}
				if (!isSerializableField(field)) {
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

	private static boolean isJdkType(final Class<?> clazz) {
		final String name = clazz.getName();
		return name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("jdk.")
				|| name.startsWith("sun.") || name.startsWith("com.sun.");
	}

	private static SurrealException duplicateName(final String name, final Field first, final Field second) {
		return new SurrealException(
				"Duplicate SurrealDB field name '" + name + "' on " + describe(first) + " and " + describe(second));
	}

	private static String describe(final Field field) {
		return field.getDeclaringClass().getName() + "." + field.getName();
	}
}
