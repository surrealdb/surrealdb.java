package com.surrealdb;

/**
 * Resource not found (table, record, namespace, database, RPC method, session).
 *
 * <p>Details use the {@code {kind, details?}} format with variants defined
 * in {@link NotFoundDetailKind}.
 *
 * @see ErrorKind#NOT_FOUND
 */
public class NotFoundException extends ServerException {

	NotFoundException(String message, java.lang.Object details, ServerException cause) {
		super(ErrorKind.NOT_FOUND, null, message, details, cause);
	}

	/**
	 * Returns the table name, if this is a table-not-found error.
	 *
	 * @return the table name, or {@code null}
	 */
	public String getTableName() {
		return detailField(getDetails(), NotFoundDetailKind.TABLE, "name");
	}

	/**
	 * Returns the record id, if this is a record-not-found error.
	 *
	 * @return the record id string, or {@code null}
	 */
	public String getRecordId() {
		return detailField(getDetails(), NotFoundDetailKind.RECORD, "id");
	}

	/**
	 * Returns the method name, if this is a method-not-found error.
	 *
	 * @return the method name, or {@code null}
	 */
	public String getMethodName() {
		return detailField(getDetails(), NotFoundDetailKind.METHOD, "name");
	}

	/**
	 * Returns the namespace name, if this is a namespace-not-found error.
	 *
	 * @return the namespace name, or {@code null}
	 */
	public String getNamespaceName() {
		return detailField(getDetails(), NotFoundDetailKind.NAMESPACE, "name");
	}

	/**
	 * Returns the database name, if this is a database-not-found error.
	 *
	 * @return the database name, or {@code null}
	 */
	public String getDatabaseName() {
		return detailField(getDetails(), NotFoundDetailKind.DATABASE, "name");
	}

	/**
	 * Returns the session id, if this is a session-not-found error.
	 *
	 * @return the session id, or {@code null}
	 */
	public String getSessionId() {
		return detailField(getDetails(), NotFoundDetailKind.SESSION, "id");
	}
}
