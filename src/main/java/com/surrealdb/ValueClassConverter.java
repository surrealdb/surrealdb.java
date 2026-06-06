package com.surrealdb;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class ValueClassConverter<T> {

	// Reflective access to record APIs (JDK 16+), looked up once.
	// The library is compiled with --release 8, so we cannot reference
	// Class.isRecord(),
	// Class.getRecordComponents() or java.lang.reflect.RecordComponent directly.
	private static final Method IS_RECORD;
	private static final Method GET_RECORD_COMPONENTS;
	private static final Method RC_GET_NAME;
	private static final Method RC_GET_TYPE;
	private static final Method RC_GET_GENERIC_TYPE;

	static {
		Method isRecord = null;
		Method getRecordComponents = null;
		Method rcGetName = null;
		Method rcGetType = null;
		Method rcGetGenericType = null;
		try {
			isRecord = Class.class.getMethod("isRecord");
			getRecordComponents = Class.class.getMethod("getRecordComponents");
			final Class<?> rcClass = Class.forName("java.lang.reflect.RecordComponent");
			rcGetName = rcClass.getMethod("getName");
			rcGetType = rcClass.getMethod("getType");
			rcGetGenericType = rcClass.getMethod("getGenericType");
		} catch (ReflectiveOperationException ignored) {
			// Pre-16 JVM: leave everything null and fall through to the POJO path.
		}
		IS_RECORD = isRecord;
		GET_RECORD_COMPONENTS = getRecordComponents;
		RC_GET_NAME = rcGetName;
		RC_GET_TYPE = rcGetType;
		RC_GET_GENERIC_TYPE = rcGetGenericType;
	}

	private final Class<T> clazz;

	ValueClassConverter(Class<T> clazz) {
		this.clazz = clazz;
	}

	private static boolean isRecord(Class<?> clazz) {
		if (IS_RECORD == null) {
			return false;
		}
		try {
			return (Boolean) IS_RECORD.invoke(clazz);
		} catch (ReflectiveOperationException e) {
			return false;
		}
	}

	private static java.lang.Object convertSingleValue(final Value value) {
		if (value.isNull())
			return null;
		if (value.isBoolean())
			return value.getBoolean();
		if (value.isDouble())
			return value.getDouble();
		if (value.isLong())
			return value.getLong();
		if (value.isString())
			return value.getString();
		if (value.isRecordId())
			return value.getRecordId();
		if (value.isGeometry())
			return value.getGeometry();
		if (value.isBigDecimal())
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

	// Type-aware single-value conversion: returns the boxed Java object that
	// Field.set / Constructor.newInstance will auto-unbox into the target slot.
	private static java.lang.Object convertSingleValueTyped(final Value value, final Class<?> type) {
		if (value.isNull()) {
			return null;
		}
		if (value.isDouble()) {
			final double d = value.getDouble();
			if (type == Float.TYPE || type == Float.class) {
				return (float) d;
			}
			return d;
		}
		if (value.isLong()) {
			final long l = value.getLong();
			if (type == Integer.TYPE || type == Integer.class) {
				return (int) l;
			}
			if (type == Short.TYPE || type == Short.class) {
				return (short) l;
			}
			return l;
		}
		if (value.isRecordId()) {
			if (type == Id.class) {
				return value.getRecordId().getId();
			}
			return value.getRecordId();
		}
		return convertSingleValue(value);
	}

	// Convert a Value into the Java object suitable for the given declared type.
	// Used by both the POJO field-setting path and the record canonical-constructor
	// path.
	private static java.lang.Object convertValueToType(final Value value, final Class<?> type, final Type genericType)
			throws ReflectiveOperationException {
		if (Value.class.equals(type)) {
			return value;
		}
		if (value.isArray()) {
			final Class<?> elementType = firstTypeArgumentRaw(genericType);
			final Type elementGenericType = firstTypeArgument(genericType);
			final List<java.lang.Object> list = new ArrayList<>();
			for (final Value element : value.getArray()) {
				list.add(convertArrayElement(element, elementType, elementGenericType));
			}
			if (type == byte[].class) {
				final byte[] bytes = new byte[list.size()];
				for (int i = 0; i < list.size(); i++) {
					final java.lang.Object e = list.get(i);
					if (e instanceof Number) {
						bytes[i] = ((Number) e).byteValue();
					} else {
						throw new SurrealException(
								"Cannot convert " + (e == null ? "null" : e.getClass()) + " to byte");
					}
				}
				return bytes;
			}
			if (Optional.class.equals(type)) {
				return Optional.of(list);
			}
			return list;
		}
		if (value.isObject()) {
			if (Map.class.isAssignableFrom(type)) {
				final Class<?> valueType = secondTypeArgumentRaw(genericType);
				if (valueType == null) {
					throw new SurrealException("Unsupported map type for: " + genericType);
				}
				final Map<String, java.lang.Object> map = new HashMap<>();
				for (final Entry mapEntry : value.getObject()) {
					final String entryKey = mapEntry.getKey();
					final Value entryValue = mapEntry.getValue();
					// todo - array support inside maps
					if (entryValue.isObject()) {
						map.put(entryKey, convert(valueType, entryValue.getObject()));
					} else {
						map.put(entryKey, convertSingleValue(entryValue));
					}
				}
				return map;
			}
			if (Optional.class.equals(type)) {
				final Class<?> innerType = firstTypeArgumentRaw(genericType);
				if (innerType == null) {
					throw new SurrealException("Unsupported Optional type for: " + genericType);
				}
				return Optional.of(convert(innerType, value.getObject()));
			}
			return convert(type, value.getObject());
		}
		// scalar value
		if (Optional.class.equals(type)) {
			final Class<?> innerType = firstTypeArgumentRaw(genericType);
			final java.lang.Object converted = innerType == null
					? convertSingleValue(value)
					: convertSingleValueTyped(value, innerType);
			return converted == null ? Optional.empty() : Optional.of(converted);
		}
		return convertSingleValueTyped(value, type);
	}

	private static java.lang.Object convertArrayElement(final Value value, final Class<?> elementType,
			final Type elementGenericType) throws ReflectiveOperationException {
		if (value.isObject()) {
			if (elementType == null) {
				throw new SurrealException("Unsupported element type for array");
			}
			return convert(elementType, value.getObject());
		}
		if (value.isArray()) {
			final List<java.lang.Object> nested = new ArrayList<>();
			for (final Value v : value.getArray()) {
				nested.add(convertArrayElement(v, elementType, elementGenericType));
			}
			return nested;
		}
		// Preserve the historical behaviour: scalar array elements are type-agnostic.
		return convertSingleValue(value);
	}

	private static Type firstTypeArgument(final Type genericType) {
		if (genericType instanceof ParameterizedType) {
			final Type[] args = ((ParameterizedType) genericType).getActualTypeArguments();
			if (args.length > 0) {
				return args[0];
			}
		}
		return null;
	}

	private static Class<?> firstTypeArgumentRaw(final Type genericType) {
		return rawType(firstTypeArgument(genericType));
	}

	private static Class<?> secondTypeArgumentRaw(final Type genericType) {
		if (genericType instanceof ParameterizedType) {
			final Type[] args = ((ParameterizedType) genericType).getActualTypeArguments();
			if (args.length > 1) {
				return rawType(args[1]);
			}
		}
		return null;
	}

	private static Class<?> rawType(final Type type) {
		if (type instanceof Class) {
			return (Class<?>) type;
		}
		if (type instanceof ParameterizedType) {
			final Type raw = ((ParameterizedType) type).getRawType();
			if (raw instanceof Class) {
				return (Class<?>) raw;
			}
		}
		return null;
	}

	private static <T> T convert(Class<T> clazz, Object source) throws ReflectiveOperationException {
		if (isRecord(clazz)) {
			return convertRecord(clazz, source);
		}
		final T target = clazz.getConstructor().newInstance();
		initOptionalFields(clazz, target);
		final Map<String, Field> fields = SurrealFieldNames.inheritedFieldsBySurrealName(clazz);
		for (final Entry entry : source) {
			final String key = entry.getKey();
			final Value value = entry.getValue();
			final Field field = fields.get(key);
			if (field == null) {
				// Safe to ignore: source has a key with no matching field.
				continue;
			}
			field.setAccessible(true);
			final java.lang.Object converted = convertValueToType(value, field.getType(), field.getGenericType());
			if (converted == null && field.getType().isPrimitive()) {
				// Leave primitive fields at their default value; setting null would throw.
				continue;
			}
			field.set(target, converted);
		}
		return target;
	}

	private static <T> T convertRecord(final Class<T> clazz, final Object source) throws ReflectiveOperationException {
		if (GET_RECORD_COMPONENTS == null) {
			// Should be unreachable: isRecord() returned true, so the JVM exposes
			// RecordComponent.
			throw new SurrealException("Record reflection APIs unavailable for " + clazz.getName());
		}
		final java.lang.Object[] componentsArray = (java.lang.Object[]) GET_RECORD_COMPONENTS.invoke(clazz);
		final int count = componentsArray.length;
		final String[] names = new String[count];
		final Class<?>[] types = new Class<?>[count];
		final Type[] genericTypes = new Type[count];
		for (int i = 0; i < count; i++) {
			final java.lang.Object rc = componentsArray[i];
			final String componentName = (String) RC_GET_NAME.invoke(rc);
			names[i] = recordComponentSurrealName(clazz, componentName);
			types[i] = (Class<?>) RC_GET_TYPE.invoke(rc);
			genericTypes[i] = (Type) RC_GET_GENERIC_TYPE.invoke(rc);
		}
		ensureUniqueRecordComponentNames(clazz, names);

		// Build a lookup of incoming entries keyed by name.
		final Map<String, Value> entries = new HashMap<>();
		for (final Entry entry : source) {
			entries.put(entry.getKey(), entry.getValue());
		}

		final java.lang.Object[] args = new java.lang.Object[count];
		for (int i = 0; i < count; i++) {
			final Value value = entries.get(names[i]);
			final Class<?> type = types[i];
			if (value == null) {
				args[i] = defaultForRecordComponent(type);
				continue;
			}
			final java.lang.Object converted = convertValueToType(value, type, genericTypes[i]);
			if (converted == null) {
				args[i] = defaultForRecordComponent(type);
			} else {
				args[i] = converted;
			}
		}

		final Constructor<T> ctor = clazz.getDeclaredConstructor(types);
		ctor.setAccessible(true);
		return ctor.newInstance(args);
	}

	private static String recordComponentSurrealName(final Class<?> clazz, final String componentName)
			throws NoSuchFieldException {
		return SurrealFieldNames.nameFor(clazz.getDeclaredField(componentName));
	}

	private static void ensureUniqueRecordComponentNames(final Class<?> clazz, final String[] names) {
		final Map<String, Boolean> seen = new HashMap<>();
		for (final String name : names) {
			if (seen.put(name, Boolean.TRUE) != null) {
				throw new SurrealException(
						"Duplicate SurrealDB field name '" + name + "' on record " + clazz.getName());
			}
		}
	}

	private static java.lang.Object defaultForRecordComponent(final Class<?> type) {
		if (Optional.class.equals(type)) {
			return Optional.empty();
		}
		if (type.isPrimitive()) {
			if (type == Boolean.TYPE)
				return Boolean.FALSE;
			if (type == Byte.TYPE)
				return (byte) 0;
			if (type == Short.TYPE)
				return (short) 0;
			if (type == Integer.TYPE)
				return 0;
			if (type == Long.TYPE)
				return 0L;
			if (type == Float.TYPE)
				return 0f;
			if (type == Double.TYPE)
				return 0d;
			if (type == Character.TYPE)
				return (char) 0;
		}
		return null;
	}

	private static <T> void initOptionalFields(Class<?> clazz, T target) throws IllegalAccessException {
		Class<?> c = clazz;
		while (c != null && c != java.lang.Object.class) {
			for (final Field field : c.getDeclaredFields()) {
				int mods = field.getModifiers();
				if (Modifier.isStatic(mods) || Modifier.isTransient(mods)) {
					continue;
				}
				if (Optional.class.equals(field.getType())) {
					field.setAccessible(true);
					if (field.get(target) == null) {
						field.set(target, Optional.empty());
					}
				}
			}
			c = c.getSuperclass();
		}
	}

	final T convert(final Value value) {
		try {
			if (value.isNone() || value.isNull())
				return null;

			if (!value.isObject())
				throw new SurrealException("Unexpected value: " + value);

			return convert(clazz, value.getObject());
		} catch (ReflectiveOperationException e) {
			throw new SurrealException("Failed to create instance of " + clazz.getName(), e);
		}
	}
}
