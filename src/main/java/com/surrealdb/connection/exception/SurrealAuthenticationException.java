package com.surrealdb.connection.exception;

/**
 * Thrown when attempting to use an RPC method that requires authentication, but the client
 * has not signed in.
 *
 * @author Khalid Alharisi
 */
public class SurrealAuthenticationException extends SurrealException {
}
