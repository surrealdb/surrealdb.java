package com.surrealdb.jdbc.exception;

import com.surrealdb.connection.exception.SurrealException;

public class SurrealJDBCInstanceConstructionException extends SurrealException {
    public SurrealJDBCInstanceConstructionException() {}

    public SurrealJDBCInstanceConstructionException(String message) {
        super(message);
    }
}
