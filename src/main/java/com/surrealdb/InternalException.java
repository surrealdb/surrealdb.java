package com.surrealdb;

/**
 * Unexpected or unknown internal error.
 *
 * @see ErrorKind#INTERNAL
 */
public class InternalException extends ServerException {

	InternalException(String message, java.lang.Object details, ServerException cause) {
		super(ErrorKind.INTERNAL, message, details, cause);
	}

	InternalException(String message, String detailsJson, ServerException cause) {
		super(ErrorKind.INTERNAL, message, detailsJson, cause);
	}
}
