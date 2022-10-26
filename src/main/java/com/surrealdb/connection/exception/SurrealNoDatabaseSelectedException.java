package com.surrealdb.connection.exception;

/**
 * Thrown when attempting to call an RPC method that requires a database to be selected,
 * but no database has been selected.
 *
 * @author Khalid Alharisi
 */
public class SurrealNoDatabaseSelectedException extends SurrealException {
}
