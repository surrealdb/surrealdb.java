package com.surrealdb.auth;

import com.google.gson.annotations.SerializedName;
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

    @SerializedName("NS")
    @NotNull String namespace;

    @SerializedName("DB")
    @NotNull String database;

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
        return namespace;
    }

    public @NotNull String getDatabase() {
        return database;
    }
}
