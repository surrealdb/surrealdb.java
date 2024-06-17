package com.surrealdb;

 class Surreal {

    final int id;

    private static native Surreal new_instance();

    private static native Surreal connect(int id, String connect);


    private Surreal(int id) {
        this.id = id;
    }

    void connect(String connect) {
        Surreal.connect(id, connect);
    }

    static {
        System.loadLibrary("surrealdb");
    }

    public static void main(String[] args) {
        try {
            Surreal surreal = Surreal.new_instance();
            surreal.connect("ws://localhost:8000");
            System.out.println("SUCCESS!");
        } catch (Exception e) {
            if (!e.getMessage().startsWith("There was an error processing a remote WS request: IO error: Connection refused ")) {
                throw e;
            }
        }
	}
}