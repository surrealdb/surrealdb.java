package com.surrealdb.connection.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Khalid Alharisi
 */
@AllArgsConstructor
@Getter
public class SurrealRecordAlreadyExitsException extends SurrealException {
    private String tableName;
    private String recordId;
}
