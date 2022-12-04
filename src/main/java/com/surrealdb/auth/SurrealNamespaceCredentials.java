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
public final class SurrealNamespaceCredentials implements SurrealAuthCredentials {

    @NotNull String user;
    @NotNull String pass;

    @SerializedName("NS")
    @NotNull String namespace;

    public static @NotNull SurrealNamespaceCredentials from(@NotNull String user, @NotNull String password, @NotNull String namespace) {
        return new SurrealNamespaceCredentials(user, password, namespace);
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
}
