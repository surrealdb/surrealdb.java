package com.surrealdb;

public class Response implements AutoCloseable {

    private final long id;

    Response(long id) {
        this.id = id;
    }

    private static native void deleteInstance(long id);

    private native long take(long id, int num);

    public Result take(int num) {
        return new Result(take(id, num));
    }

    @Override
    public void close() {
        deleteInstance(id);
    }
}
