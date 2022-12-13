package com.surrealdb.exception;

import lombok.experimental.StandardException;

/**
 * Thrown when attempting to use an RPC method that requires authentication, but the client
 * has not signed in.
 *
 * @author Khalid Alharisi
 */
@StandardException
public final class SurrealAuthenticationException extends SurrealException {

}
