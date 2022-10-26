package com.surrealdb.connection.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Thrown when attempting to create a record that already exists.
 *
 * @author Khalid Alharisi
 */
@AllArgsConstructor
@Getter
public class SurrealRecordAlreadyExistsException extends SurrealException {

    private String tableName;
    private String recordId;

}
