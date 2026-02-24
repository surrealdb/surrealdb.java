package com.surrealdb;

/**
 * Duplicate resource (table, record, namespace, database, session).
 *
 * <p>Details use the {@code {kind, details?}} format with variants defined
 * in {@link AlreadyExistsDetailKind}.
 *
 * @see ErrorKind#ALREADY_EXISTS
 */
public class AlreadyExistsException extends ServerException {

	AlreadyExistsException(String message, java.lang.Object details, ServerException cause) {
		super(ErrorKind.ALREADY_EXISTS, message, details, cause);
	}

	AlreadyExistsException(String message, String detailsJson, ServerException cause) {
		super(ErrorKind.ALREADY_EXISTS, message, detailsJson, cause);
	}

	/**
	 * Returns the record id, if this is a duplicate record error.
	 *
	 * @return the record id string, or {@code null}
	 */
	public String getRecordId() {
		return detailField(getDetails(), AlreadyExistsDetailKind.RECORD, "id");
	}

	/**
	 * Returns the table name, if this is a duplicate table error.
	 *
	 * @return the table name, or {@code null}
	 */
	public String getTableName() {
		return detailField(getDetails(), AlreadyExistsDetailKind.TABLE, "name");
	}

	/**
	 * Returns the session id, if this is a duplicate session error.
	 *
	 * @return the session id, or {@code null}
	 */
	public String getSessionId() {
		return detailField(getDetails(), AlreadyExistsDetailKind.SESSION, "id");
	}

	/**
	 * Returns the namespace name, if this is a duplicate namespace error.
	 *
	 * @return the namespace name, or {@code null}
	 */
	public String getNamespaceName() {
		return detailField(getDetails(), AlreadyExistsDetailKind.NAMESPACE, "name");
	}

	/**
	 * Returns the database name, if this is a duplicate database error.
	 *
	 * @return the database name, or {@code null}
	 */
	public String getDatabaseName() {
		return detailField(getDetails(), AlreadyExistsDetailKind.DATABASE, "name");
	}
}
