package com.surrealdb;

class ValueBuilder extends Native {

    ValueBuilder(long ptr) {
        super(ptr);
    }

    static protected native long newValueString(String string);

    static protected native long newValueBoolean(boolean b);

    static protected native long newValueDouble(double d);

    static protected native long newValueLong(long l);

    static <T> Value convert(final T object) {
        if (object == null) {
            return null;
        }
        if (object instanceof String) {
            return new Value(newValueString((String) object));
        }
        if (object instanceof Double) {
            return new Value(newValueDouble((Double) object));
        }
        if (object instanceof Float) {
            return new Value(newValueDouble((Float) object));
        }
        if (object instanceof Long) {
            return new Value(newValueLong((Long) object));
        }
        if (object instanceof Integer) {
            return new Value(newValueLong((Integer) object));
        }
        if (object instanceof Short) {
            return new Value(newValueLong((Short) object));
        }
        if (object instanceof Boolean) {
            return new Value(newValueBoolean((Boolean) object));
        }
        throw new SurrealException("Type not supported: " + object.getClass().getName());
    }

    final protected native boolean deleteInstance(long ptr);
}
