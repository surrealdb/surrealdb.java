package com.surrealdb.connection.exception;

/**
 * @author Khalid Alharisi
 */
public class SurrealException extends RuntimeException {

	public SurrealException() {}

	public SurrealException(String message) {
		super(message);
	}
}
