package com.surrealdb.exception;

import lombok.experimental.StandardException;

/**
 * Thrown when attempting to call an RPC method that requires a database to be selected,
 * but no database has been selected.
 *
 * @author Khalid Alharisi
 */
@StandardException
public final class SurrealNoDatabaseSelectedException extends SurrealException {

}
