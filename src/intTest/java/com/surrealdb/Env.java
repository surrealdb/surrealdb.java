package com.surrealdb;

import java.util.Optional;

public class Env {
    public static final Optional<String> envHost = Optional.ofNullable(System.getenv(TestEnvVars.SURREALDB_JAVA_HOST)).filter(str -> !str.isBlank());
    public static final Optional<Integer> envPort = Optional.ofNullable(System.getenv(TestEnvVars.SURREALDB_JAVA_PORT)).map(strPort -> {
        try {
            return Integer.parseInt(strPort);
        } catch (NumberFormatException e) {
            return null;
        }
    });
}
