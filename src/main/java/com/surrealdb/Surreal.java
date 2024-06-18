package com.surrealdb;

public class Surreal implements AutoCloseable {

    static {
        System.loadLibrary("surrealdb");
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