package com.surrealdb;

import java.util.Iterator;
import java.util.Objects;

public class EntryIterator extends Native implements Iterator<Entry> {

    EntryIterator(long ptr) {
        super(ptr);
    }

    private static native boolean hasNext(long ptr);

    private static native long next(long ptr);

    @Override
    final String toString(long ptr) {
        return "EntryIterator[ptr=" + ptr + "]";
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
    final native boolean deleteInstance(long ptr);

    @Override
    public boolean hasNext() {
        return hasNext(getPtr());
    }

    @Override
    public Entry next() {
        return new Entry(next(getPtr()));
    }


}
