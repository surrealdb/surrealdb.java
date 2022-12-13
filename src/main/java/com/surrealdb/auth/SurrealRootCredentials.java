package com.surrealdb.auth;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
public final class SurrealRootCredentials implements SurrealAuthCredentials {

    @NotNull String user;
    @NotNull String pass;

    public static @NotNull SurrealRootCredentials from(@NotNull String user, @NotNull String password) {
        return new SurrealRootCredentials(user, password);
    }

    public @NotNull String getUser() {
        return user;
    }

    public @NotNull String getPassword() {
        return pass;
    }
}
