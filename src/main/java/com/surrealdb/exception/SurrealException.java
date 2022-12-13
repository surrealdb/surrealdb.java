package com.surrealdb.exception;

import lombok.experimental.StandardException;

/**
 * {@code SurrealException} is the base class for all exceptions thrown by the SurrealDB Java client.
 *
 * @author Khalid Alharisi
 */
@StandardException
public sealed class SurrealException extends RuntimeException permits
    SurrealAuthenticationException,
    SurrealConnectionTimeoutException,
    SurrealNoDatabaseSelectedException,
    SurrealNotConnectedException,
    SurrealRecordAlreadyExistsException {
}
