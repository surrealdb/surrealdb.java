package com.surrealdb;

import java.util.List;

public class Response implements AutoCloseable {

    private final long id;

    Response(long id) {
        this.id = id;
    }

    private static native void delete_instance(long id);

    private native <T> List<T> take(long id, int num, Class<T> recordType) throws SurrealDBException;

    public <T> List<T> take(int num, Class<T> recordType) throws SurrealDBException {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public void close() {
        delete_instance(id);
    }
}
