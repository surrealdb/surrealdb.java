package com.surrealdb.driver.model;

import lombok.Value;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * @author Khalid Alharisi
 */
@Value
public class SignIn {

    String user;
    String password;
    @Nullable
    String namespace;
    @Nullable
    String database;
    @Nullable
    String scope;

    public Optional<String> getNamespace() {
        return Optional.ofNullable(namespace);
    }

    public Optional<String> getDatabase() {
        return Optional.ofNullable(database);
    }

    public Optional<String> getScope() {
        return Optional.ofNullable(scope);
    }
}
