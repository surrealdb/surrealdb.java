package com.surrealdb;

/**
 * Feature or configuration not supported (e.g. live queries, GraphQL).
 *
 * <p>Details use the {@code {kind, details?}} format with variants defined
 * in {@link ConfigurationDetailKind}.
 *
 * @see ErrorKind#CONFIGURATION
 */
public class ConfigurationException extends ServerException {

	ConfigurationException(String message, java.lang.Object details, ServerException cause) {
		super(ErrorKind.CONFIGURATION, null, message, details, cause);
	}

	/**
	 * Returns {@code true} when the error indicates that live queries are not
	 * supported.
	 *
	 * @return whether the detail kind is {@code LiveQueryNotSupported}
	 */
	public boolean isLiveQueryNotSupported() {
		return hasDetailKey(getDetails(), ConfigurationDetailKind.LIVE_QUERY_NOT_SUPPORTED);
	}
}
