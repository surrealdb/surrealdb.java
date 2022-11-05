package com.surrealdb.connection.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * Thrown when attempting to create a record that already exists.
 *
 * @author Khalid Alharisi
 */
@AllArgsConstructor
@Getter
public class SurrealRecordAlreadyExistsException extends SurrealException {

    private @NotNull String tableName;
    private @NotNull String recordId;

}
