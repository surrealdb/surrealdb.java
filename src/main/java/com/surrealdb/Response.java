package com.surrealdb;

public class Response extends Native {

    Response(long ptr) {
        super(ptr);
    }

    final protected native boolean deleteInstance(long ptr);

    private native long take(long ptr, int num);

    public Value take(int num) {
        return new Value(take(getPtr(), num));
    }

}
