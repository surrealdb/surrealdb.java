package com.surrealdb.exception;

/**
 * Thrown when attempting to use an RPC method that requires authentication, but the client
 * has not signed in.
 *
 * @author Khalid Alharisi
 */
public final class SurrealAuthenticationException extends SurrealException {

    public SurrealAuthenticationException() {
        super("Not authenticated");
    }
}
