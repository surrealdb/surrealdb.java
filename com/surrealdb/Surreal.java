package com.surrealdb;

import java.util.*;

 class Surreal {

    final int id;

    static native Surreal new_instance();

    static native void connect(String connect);

    private Surreal(int id) {
        this.id = id;
    }

    static {
        System.loadLibrary("surrealdb");
    }

    public static void main(String[] args) {
        Surreal surreal = Surreal.new_instance();
        surreal.connect("ws://localhost:8000");
	}
}