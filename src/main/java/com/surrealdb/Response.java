package com.surrealdb;

public class Response implements AutoCloseable {

    private long id;

    Response(long id) {
        this.id = id;
    }

    private static native void deleteInstance(long id);

    private native long take(long id, int num);

    public Value take(int num) {
        return new Value(take(id, num));
    }

    @Override
    public void close() {
        deleteInstance(id);
        id = 0;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }
}
