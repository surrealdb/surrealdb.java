package com.surrealdb;

/**
 * User-thrown error (e.g. from THROW in SurrealQL).
 *
 * @see ErrorKind#THROWN
 */
public class ThrownException extends ServerException {

	ThrownException(String message, java.lang.Object details, ServerException cause) {
		super(ErrorKind.THROWN, null, message, details, cause);
	}
}
