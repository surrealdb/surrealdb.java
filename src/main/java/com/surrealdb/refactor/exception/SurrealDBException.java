package com.surrealdb.refactor.exception;

/**
 * Baseline exception in the db driver
 */
public class SurrealDBException extends RuntimeException {
    public SurrealDBException(String message) {
        super(message);
    }
}
