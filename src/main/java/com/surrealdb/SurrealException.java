package com.surrealdb;

/**
 * The SurrealException class is a custom RuntimeException used to signal exceptional conditions.
 */
public class SurrealException extends RuntimeException {

    SurrealException(String message) {
        super(message);
    }

    SurrealException(String message, Throwable cause) {
        super(message, cause);
    }

}
