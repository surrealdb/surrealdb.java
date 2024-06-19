package com.surrealdb;

import static com.surrealdb.Loader.load_native;

public class Surreal implements AutoCloseable {

    static {
        load_native();
    }

    final int id;

    private Surreal(int id) {
        this.id = id;
    }

    public static native Surreal new_instance();

    public native Surreal connect(String connect);

    @Override
    public native void close() throws Exception;
}