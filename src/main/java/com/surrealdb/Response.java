package com.surrealdb;

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
    final protected native String toString(long ptr);

    @Override
    final protected native int hashCode(long ptr);

    @Override
    final protected native boolean equals(long ptr1, long ptr2);

}
