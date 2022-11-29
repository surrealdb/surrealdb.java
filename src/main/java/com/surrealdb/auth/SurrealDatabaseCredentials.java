package com.surrealdb.auth;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
public final class SurrealDatabaseCredentials implements SurrealAuthCredentials {

    @NotNull String user;
    @NotNull String pass;
    @NotNull String NS;
    @NotNull String DB;

    public static @NotNull SurrealDatabaseCredentials from(@NotNull String user, @NotNull String password, @NotNull String namespace, @NotNull String database) {
        return new SurrealDatabaseCredentials(user, password, namespace, database);
    }

    public @NotNull String getUser() {
        return user;
    }

    public @NotNull String getPassword() {
        return pass;
    }

    public @NotNull String getNamespace() {
        return NS;
    }

    public @NotNull String getDatabase() {
        return DB;
    }
}
