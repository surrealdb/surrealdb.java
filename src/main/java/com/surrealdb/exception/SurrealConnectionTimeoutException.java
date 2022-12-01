package com.surrealdb.exception;

/**
 * Thrown when the connection to the database times out.
 *
 * @author Khalid Alharisi
 */
public final class SurrealConnectionTimeoutException extends SurrealException {

    public SurrealConnectionTimeoutException() {
        super("Connection timed out");
    }
}