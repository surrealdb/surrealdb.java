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
public final class SurrealScopeCredentials implements SurrealAuthCredentials {

    @SerializedName("NS")
    @NotNull String namespace;

    @SerializedName("DB")
    @NotNull String database;

    @SerializedName("SC")
    @NotNull String scope;

    public static @NotNull SurrealScopeCredentials from(@NotNull String namespace, @NotNull String database, @NotNull String scope) {
        return new SurrealScopeCredentials(namespace, database, scope);
    }

    public @NotNull String getNamespace() {
        return namespace;
    }

    public @NotNull String getDatabase() {
        return database;
    }

    public @NotNull String getScope() {
        return scope;
    }
}
