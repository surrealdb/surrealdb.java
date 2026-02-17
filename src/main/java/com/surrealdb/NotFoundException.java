package com.surrealdb;

/**
 * Resource not found (table, record, namespace, database, RPC method).
 *
 * @see ErrorKind#NOT_FOUND
 */
public class NotFoundException extends ServerException {

	NotFoundException(String message, java.lang.Object details, ServerException cause) {
		super(ErrorKind.NOT_FOUND, message, details, cause);
	}

	NotFoundException(String message, String detailsJson, ServerException cause) {
		super(ErrorKind.NOT_FOUND, message, detailsJson, cause);
	}

	/**
	 * Returns the table name, if this is a table-not-found error.
	 *
	 * @return the table name, or {@code null}
	 */
	public String getTableName() {
		return getNestedString(getDetails(), "Table", "name");
	}

	/**
	 * Returns the record id, if this is a record-not-found error.
	 *
	 * @return the record id string, or {@code null}
	 */
	public String getRecordId() {
		return getNestedString(getDetails(), "Record", "id");
	}

	/**
	 * Returns the method name, if this is a method-not-found error.
	 *
	 * @return the method name, or {@code null}
	 */
	public String getMethodName() {
		return getNestedString(getDetails(), "Method", "name");
	}

	/**
	 * Returns the namespace name, if this is a namespace-not-found error.
	 *
	 * @return the namespace name, or {@code null}
	 */
	public String getNamespaceName() {
		return getNestedString(getDetails(), "Namespace", "name");
	}

	/**
	 * Returns the database name, if this is a database-not-found error.
	 *
	 * @return the database name, or {@code null}
	 */
	public String getDatabaseName() {
		return getNestedString(getDetails(), "Database", "name");
	}
}
