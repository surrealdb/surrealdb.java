package com.surrealdb.driver;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SurrealTable<T> {

    @NotNull String name;
    @NotNull Class<T> type;

    public static <T> @NotNull SurrealTable<T> of(@NotNull String name, @NotNull Class<T> type) {
        return new SurrealTable<>(name, type);
    }

    public <U> @NotNull SurrealTable<U> withType(@NotNull Class<U> type) {
        return new SurrealTable<>(name, type);
    }

    @NotNull String makeThing(String record) {
        return name + ":" + record;
    }
}
