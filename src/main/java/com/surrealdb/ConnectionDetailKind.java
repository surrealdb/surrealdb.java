package com.surrealdb;

/**
 * Detail kind constants for connection errors.
 *
 * @see ErrorKind#CONNECTION
 */
public final class ConnectionDetailKind {

	public static final String UNINITIALISED = "Uninitialised";
	public static final String ALREADY_CONNECTED = "AlreadyConnected";

	private ConnectionDetailKind() {
	}
}
