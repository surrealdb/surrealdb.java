package com.surrealdb;

/**
 * @author Khalid Alharisi
 */
public class TestUtils {

    private static final String HOST = "localhost";
    private static final int PORT = 8000;

    private static final String USERNAME = "root";
    private static final String PASSWORD = "root";

    private static final String NAMESPACE = "test";
    private static final String DATABASE = "test";

    public static String getHost(){
        return HOST;
    }

    public static int getPort(){
        return PORT;
    }

    public static String getUsername(){
        return USERNAME;
    }

    public static String getPassword(){
        return PASSWORD;
    }

    public static String getNamespace(){
        return NAMESPACE;
    }

    public static String getDatabase(){
        return DATABASE;
    }

}
