package com.surrealdb;

/**
 * Feature or configuration not supported (e.g. live queries, GraphQL).
 *
 * @see ErrorKind#CONFIGURATION
 */
public class ConfigurationException extends ServerException {

	ConfigurationException(String message, java.lang.Object details, ServerException cause) {
		super(ErrorKind.CONFIGURATION, message, details, cause);
	}

	ConfigurationException(String message, String detailsJson, ServerException cause) {
		super(ErrorKind.CONFIGURATION, message, detailsJson, cause);
	}

	/**
	 * Returns {@code true} when the error indicates that live queries are not
	 * supported.
	 *
	 * @return whether the detail is {@code "LiveQueryNotSupported"}
	 */
	public boolean isLiveQueryNotSupported() {
		return hasDetailKey(getDetails(), "LiveQueryNotSupported");
	}
}
