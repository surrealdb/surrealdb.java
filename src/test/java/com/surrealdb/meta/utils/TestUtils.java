package com.surrealdb.meta.utils;

import com.surrealdb.SurrealClientSettings;
import com.surrealdb.SurrealConnectionProtocol;
import com.surrealdb.auth.SurrealAuthCredentials;
import com.surrealdb.auth.SurrealRootCredentials;
import lombok.experimental.UtilityClass;

/**
 * @author Khalid Alharisi
 */
@UtilityClass
public class TestUtils {

    private static final String HOST = System.getenv("TEST_SURREAL_HOST");
    private static final int PORT = Integer.parseInt(System.getenv("TEST_SURREAL_PORT"));

    private static final String USERNAME = System.getenv("TEST_SURREAL_USERNAME");
    private static final String PASSWORD = System.getenv("TEST_SURREAL_PASSWORD");

    private static final String NAMESPACE = System.getenv("TEST_SURREAL_NAMESPACE");
    private static final String DATABASE = System.getenv("TEST_SURREAL_DATABASE");

    public static SurrealClientSettings.Builder createClientSettingsBuilderWithDefaults() {
        SurrealClientSettings.Builder builder = SurrealClientSettings.builder();
        builder.setUriFromComponents(getProtocol(), HOST, PORT);
        return builder;
    }

    public static SurrealClientSettings getClientSettings() {
        return createClientSettingsBuilderWithDefaults().build();
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
