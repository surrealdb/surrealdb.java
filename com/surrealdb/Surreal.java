package com.surrealdb;

 public class Surreal implements AutoCloseable {

    final int id;

    public static native Surreal new_instance();

    public native Surreal connect(String connect);

    private Surreal(int id) {
        this.id = id;
    }

    @Override
    public native void close() throws Exception;

    static {
        System.loadLibrary("surrealdb");
    }

    public static void main(String[] args) throws Exception {
        try (Surreal surreal = Surreal.new_instance()) {
            surreal.connect("ws://localhost:8000");
        } catch (Exception e) {
            if (!e.getMessage().startsWith("There was an error processing a remote WS request: IO error: Connection refused ")) {
                throw e;
            }
            System.out.println("EXPECTED ERROR: " + e);
        }
        System.out.println("SUCCESS!");
	}

}