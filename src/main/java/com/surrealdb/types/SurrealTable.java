package com.surrealdb.types;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

/**
 * {@code SurrealTable} is a {@code SurrealDB.Java} abstraction designed to simplify operations involving
 * SurrealDB tables. A {@code SurrealTable} ties together table name and table schema (represented by
 * a Java class).
 */
@EqualsAndHashCode
@ToString
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public sealed class SurrealTable<T extends SurrealRecord> permits SurrealEdgeTable {

    @NotNull String name;
    @NotNull Class<T> type;

    /**
     * Creates a new {@code SurrealTable} with the given name and type.
     *
     * @param name The name of the table
     * @param type The class representing the table schema
     * @param <T>  The type of the table
     * @return A new {@code SurrealTable} with the given name and type
     */
    public static <T extends SurrealRecord> @NotNull SurrealTable<T> of(@NotNull String name, @NotNull Class<T> type) {
        return new SurrealTable<>(name, type);
    }

    /**
     * Creates a new {@code SurrealTable} that shares the same name as this table, but with
     * the provided type. This is useful for when you want to update record(s) with a partial
     * representation of the record.
     *
     * @param type The class representing the table schema
     * @param <U>  The type of the table
     * @return A new {@code SurrealTable} that shares the name of this table, but has the given type
     */
    public <U extends SurrealRecord> @NotNull SurrealTable<U> withType(@NotNull Class<U> type) {
        return new SurrealTable<>(name, type);
    }

    /**
     * This will change soon.
     *
     * @param recordId The record to append to the table
     * @return A string in the format of 'table_name:recordId'
     */
    public @NotNull Id makeThing(@NotNull String recordId) {
        return Id.of(name, recordId);
    }

    /**
     * @return The name of the table
     */
    public @NotNull String getName() {
        return name;
    }

    /**
     * @return The class representing the table schema
     */
    public @NotNull Class<T> getType() {
        return type;
    }
}
