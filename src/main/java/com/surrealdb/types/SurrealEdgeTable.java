package com.surrealdb.types;

import org.jetbrains.annotations.NotNull;

public final class SurrealEdgeTable<T extends SurrealEdgeRecord> extends SurrealTable<T>{

    private SurrealEdgeTable(@NotNull String name, @NotNull Class<T> type) {
        super(name, type);
    }

    public static <T extends SurrealEdgeRecord> @NotNull SurrealEdgeTable<T> ofTemp(@NotNull String name, @NotNull Class<T> type) {
        return new SurrealEdgeTable<>(name, type);
    }
}
