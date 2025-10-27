package com.surrealdb;

import java.util.Iterator;
import java.util.Objects;

/**
 * The ValueIterator class provides an iterator for the Value type, allowing iteration
 * over a collection of Value objects, typically used within the context of array
 * structures that extend the functionality provided by the library.
 * <p>
 * This class implements the Iterator interface for the
 * Value type, enabling standard iteration mechanisms such as hasNext and next methods.
 * <p>
 * Methods:
 * - hasNext(): Checks if there are more elements in the collection to iterate over.
 * - next(): Returns the next Value in the iteration.
 */
public class ValueIterator extends Native implements Iterator<Value> {

    ValueIterator(long ptr) {
        super(ptr);
    }

    private static native boolean hasNext(long ptr);

    private static native long next(long ptr);

    @Override
    final String toString(long ptr) {
        return getClass().getName() + "[ptr=" + ptr + "]";
    }

    @Override
    final int hashCode(long ptr) {
        return Objects.hashCode(ptr);
    }

    @Override
    final boolean equals(long ptr1, long ptr2) {
        return ptr1 == ptr2;
    }

    @Override
    final native void deleteInstance(long ptr);

    @Override
    public boolean hasNext() {
        return hasNext(getPtr());
    }

    @Override
    public Value next() {
        return new Value(next(getPtr()));
    }
}
