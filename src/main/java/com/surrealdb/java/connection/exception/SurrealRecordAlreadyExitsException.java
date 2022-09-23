package com.surrealdb.java.connection.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SurrealRecordAlreadyExitsException extends SurrealException {
    private String tableName;
    private String recordId;
}
