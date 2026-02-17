package com.surrealdb;

/**
 * Invalid input: parse error, invalid request or parameters, bad input values.
 *
 * @see ErrorKind#VALIDATION
 */
public class ValidationException extends ServerException {

	ValidationException(String message, java.lang.Object details, ServerException cause) {
		super(ErrorKind.VALIDATION, message, details, cause);
	}

	ValidationException(String message, String detailsJson, ServerException cause) {
		super(ErrorKind.VALIDATION, message, detailsJson, cause);
	}

	/**
	 * Returns {@code true} when the error is a parse error.
	 *
	 * @return whether the detail is {@code "Parse"}
	 */
	public boolean isParseError() {
		return hasDetailKey(getDetails(), "Parse");
	}

	/**
	 * Returns the invalid parameter name, if this is an
	 * {@code InvalidParameter} error.
	 *
	 * @return the parameter name, or {@code null}
	 */
	public String getParameterName() {
		return getNestedString(getDetails(), "InvalidParameter", "name");
	}
}
