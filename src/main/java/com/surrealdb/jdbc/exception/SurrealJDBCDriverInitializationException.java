package com.surrealdb.jdbc.exception;

import com.surrealdb.connection.exception.SurrealException;

public class SurrealJDBCDriverInitializationException extends SurrealException {
    public SurrealJDBCDriverInitializationException() {}

    public SurrealJDBCDriverInitializationException(String message) {
        super(message);
    }
}
