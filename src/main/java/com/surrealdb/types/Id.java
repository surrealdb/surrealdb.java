package com.surrealdb.types;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
public final class Id {

    @NotNull String table;
    @NotNull String recordId;

    public static @NotNull Id of(@NotNull String table, @NotNull String id) {
        return new Id(table, id);
    }

    public static @NotNull Id parse(@NotNull String combinedId) {
        int separatorIndex = combinedId.indexOf(':');

        if (separatorIndex == -1) {
            throw new IllegalArgumentException("Invalid combinedId: " + combinedId);
        }

        String table = combinedId.substring(0, separatorIndex);
        String recordId = combinedId.substring(separatorIndex + 1);

        return new Id(table, recordId);
    }

    public @NotNull String toCombinedId() {
        return table + ":" + recordId;
    }

    public @NotNull String getTable() {
        return table;
    }

    public @NotNull String getRecordId() {
        return recordId;
    }
}
