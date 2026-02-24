package com.surrealdb;

/**
 * Detail kind constants for not-found errors.
 *
 * @see NotFoundException
 */
public final class NotFoundDetailKind {

	public static final String METHOD = "Method";
	public static final String SESSION = "Session";
	public static final String TABLE = "Table";
	public static final String RECORD = "Record";
	public static final String NAMESPACE = "Namespace";
	public static final String DATABASE = "Database";
	public static final String TRANSACTION = "Transaction";

	private NotFoundDetailKind() {
	}
}
