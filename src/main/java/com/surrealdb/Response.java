package com.surrealdb;

import java.util.Objects;

public class Response extends Native {

    Response(long ptr) {
        super(ptr);
    }

    @Override
    final protected native boolean deleteInstance(long ptr);

    private native long take(long ptr, int num);

    public Value take(int num) {
        return new Value(take(getPtr(), num));
    }

    @Override
    protected String toString(long ptr) {
        return getClass().getName() + "[ptr=" + ptr + "]";
    }

    @Override
    protected int hashCode(long ptr) {
        return Objects.hashCode(ptr);
    }

    @Override
    protected boolean equals(long ptr1, long ptr2) {
        return ptr1 == ptr2;
    }

}
