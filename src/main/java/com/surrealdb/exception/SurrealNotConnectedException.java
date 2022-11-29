package com.surrealdb.exception;

/**
 * Thrown when attempting to use an RPC method before connecting to the database.
 *
 * @author Khalid Alharisi
 */
public final class SurrealNotConnectedException extends SurrealException {

    public SurrealNotConnectedException() {
        super("Not connected to the database");
    }
}
