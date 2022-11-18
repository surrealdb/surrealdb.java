package com.surrealdb.connection.exception;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Thrown when attempting to create a record that already exists.
 *
 * @author Khalid Alharisi
 */
@Getter
public final class SurrealRecordAlreadyExistsException extends SurrealException {

    private @NotNull String tableName;
    private @NotNull String recordId;

    public SurrealRecordAlreadyExistsException(@NotNull String tableName, @NotNull String recordId) {
        super("Record `" + tableName + ":" + recordId + "` already exists");

        this.tableName = tableName;
        this.recordId = recordId;
    }
}
