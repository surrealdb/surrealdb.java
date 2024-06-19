package com.surrealdb;

import org.scijava.nativelib.NativeLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Surreal implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(Surreal.class);

    static {
        try {
            NativeLoader.loadLibrary("surrealdb");  // Base name of your library
        } catch (Exception e) {
            logger.warn("Unable to load SurrealDB's library", e);
        }
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