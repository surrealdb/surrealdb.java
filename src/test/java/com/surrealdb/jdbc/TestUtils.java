package com.surrealdb.jdbc;

import java.util.Properties;

/**
 * @author Khalid Alharisi
 */
public class TestUtils {
    private static final String HOST = System.getenv("TEST_SURREAL_HOST");
    private static final int PORT = Integer.parseInt(System.getenv("TEST_SURREAL_PORT"));

    private static final String USERNAME = System.getenv("TEST_SURREAL_USERNAME");
    private static final String PASSWORD = System.getenv("TEST_SURREAL_PASSWORD");

    private static final String NAMESPACE = System.getenv("TEST_SURREAL_NAMESPACE");
    private static final String DATABASE = System.getenv("TEST_SURREAL_DATABASE");
	private static final String SCOPE = System.getenv("TEST_SURREAL_SCOPE");
    private static final String JWT_TOKEN = System.getenv("TEST_SURREAL_TOKEN");

    private static final Properties DRIVER_PROPERTIES = new Properties();

    // These two are for dev and definitely open for discussion :)
    private static final boolean USE_TLS_DRIVER = true;
    private static final boolean USE_ASYNC_DRIVER = true;

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

	public static String getToken() { return JWT_TOKEN; }

	public static String getScope() { return SCOPE; }

    public static boolean getUseAsyncDriver() {
        return USE_ASYNC_DRIVER;
    }
    public static boolean getUseTlsDriver() {
        return USE_TLS_DRIVER;
    }

    public static Properties getDriverProperties() {
        DRIVER_PROPERTIES.setProperty("host", HOST);
        DRIVER_PROPERTIES.setProperty("port", String.valueOf(PORT));
        DRIVER_PROPERTIES.setProperty("tls", String.valueOf(true));
        DRIVER_PROPERTIES.setProperty("async", String.valueOf(true));
        DRIVER_PROPERTIES.setProperty("dbname", DATABASE);

        return DRIVER_PROPERTIES;
    }


}
