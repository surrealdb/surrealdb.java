package com.surrealdb;

/**
 * Duplicate resource (table, record, namespace).
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
		return getNestedString(getDetails(), "Record", "id");
	}

	/**
	 * Returns the table name, if this is a duplicate table error.
	 *
	 * @return the table name, or {@code null}
	 */
	public String getTableName() {
		return getNestedString(getDetails(), "Table", "name");
	}
}
