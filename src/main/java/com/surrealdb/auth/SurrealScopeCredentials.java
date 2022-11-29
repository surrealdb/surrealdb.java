package com.surrealdb.auth;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
@EqualsAndHashCode
public final class SurrealScopeCredentials implements SurrealAuthCredentials {

    @NotNull String NS;
    @NotNull String DB;
    @NotNull String SC;

    public static @NotNull SurrealScopeCredentials from(@NotNull String namespace, @NotNull String database, @NotNull String scope) {
        return new SurrealScopeCredentials(namespace, database, scope);
    }

    public @NotNull String getNamespace() {
        return NS;
    }

    public @NotNull String getDatabase() {
        return DB;
    }

    public @NotNull String getScope() {
        return SC;
    }
}
