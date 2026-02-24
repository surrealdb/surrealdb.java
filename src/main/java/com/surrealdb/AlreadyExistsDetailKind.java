package com.surrealdb;

/**
 * Detail kind constants for already-exists errors.
 *
 * @see AlreadyExistsException
 */
public final class AlreadyExistsDetailKind {

	public static final String SESSION = "Session";
	public static final String TABLE = "Table";
	public static final String RECORD = "Record";
	public static final String NAMESPACE = "Namespace";
	public static final String DATABASE = "Database";

	private AlreadyExistsDetailKind() {
	}
}
