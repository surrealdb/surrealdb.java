package com.surrealdb.meta.utils;

import com.surrealdb.connection.SurrealConnectionProtocol;
import com.surrealdb.connection.SurrealConnectionSettings;
import com.surrealdb.driver.auth.SurrealAuthCredentials;
import com.surrealdb.driver.auth.SurrealRootCredentials;

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

    public static SurrealConnectionSettings.Builder createConnectionSettingsBuilderWithDefaults() {
        return SurrealConnectionSettings.builder()
            .setUriFromComponents(getProtocol(), HOST, PORT);
    }

    public static SurrealConnectionSettings getConnectionSettings() {
        return createConnectionSettingsBuilderWithDefaults().build();
    }

    public static SurrealConnectionProtocol getProtocol() {
        return SurrealConnectionProtocol.WEB_SOCKET;
    }

    public static SurrealAuthCredentials getAuthCredentials() {
        return SurrealRootCredentials.from(USERNAME, PASSWORD);
    }

    public static String getHost() {
        return HOST;
    }

    public static int getPort() {
        return PORT;
    }

    public static String getUsername() {
        return USERNAME;
    }

    public static String getPassword() {
        return PASSWORD;
    }

    public static String getNamespace() {
        return NAMESPACE;
    }

    public static String getDatabase() {
        return DATABASE;
    }
}
