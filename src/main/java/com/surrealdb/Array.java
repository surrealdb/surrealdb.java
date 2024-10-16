package com.surrealdb;

import java.util.Iterator;

/**
 * The Array class represents a native array structure and provides methods to
 * interact with and retrieve values from the array.
 * It implements the Iterable interface for Value type.
 */
public class Array extends Native implements Iterable<Value> {


    Array(long ptr) {
        super(ptr);
    }

    private static native String toPrettyString(long ptr);

    private static native long get(long ptr, int idx);

    private static native int len(long ptr);

    private static native long iterator(long ptr);

    private static native long synchronizedIterator(long ptr);

    @Override
    final native String toString(long ptr);

    @Override
    final native int hashCode(long ptr);

    @Override
    final native boolean equals(long ptr1, long ptr2);

    final public String toPrettyString() {
        return toPrettyString(getPtr());
    }

    final public Value get(int idx) {
        return new Value(get(getPtr(), idx));
    }

    final public int len() {
        return len(getPtr());
    }

    @Override
    final native boolean deleteInstance(long ptr);

    @Override
    final public Iterator<Value> iterator() {
        return new ValueIterator(iterator(getPtr()));
    }

    final public <T> Iterator<T> iterator(Class<T> clazz) {
        return new ValueObjectIterator<>(clazz, iterator());
    }

    final public Iterator<Value> synchronizedIterator() {
        return new SynchronizedValueIterator(synchronizedIterator(getPtr()));
    }

    final public <T> Iterator<T> synchronizedIterator(Class<T> clazz) {
        return new ValueObjectIterator<>(clazz, synchronizedIterator());
    }
}

