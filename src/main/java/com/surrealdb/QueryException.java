package com.surrealdb;

import java.util.Map;

/**
 * Query execution failure (not executed, timed out, cancelled).
 *
 * @see ErrorKind#QUERY
 */
public class QueryException extends ServerException {

	QueryException(String message, java.lang.Object details, ServerException cause) {
		super(ErrorKind.QUERY, message, details, cause);
	}

	QueryException(String message, String detailsJson, ServerException cause) {
		super(ErrorKind.QUERY, message, detailsJson, cause);
	}

	/**
	 * Returns {@code true} when the query was not executed
	 * (e.g. a previous statement in a multi-statement query failed).
	 *
	 * @return whether the detail is {@code "NotExecuted"}
	 */
	public boolean isNotExecuted() {
		return hasDetailKey(getDetails(), "NotExecuted");
	}

	/**
	 * Returns {@code true} when the query timed out.
	 *
	 * @return whether the detail key is {@code "TimedOut"}
	 */
	public boolean isTimedOut() {
		return hasDetailKey(getDetails(), "TimedOut");
	}

	/**
	 * Returns {@code true} when the query was cancelled.
	 *
	 * @return whether the detail is {@code "Cancelled"}
	 */
	public boolean isCancelled() {
		return hasDetailKey(getDetails(), "Cancelled");
	}

	/**
	 * Returns the timeout duration when the query timed out, as a map
	 * containing {@code "secs"} and {@code "nanos"} keys.
	 *
	 * @return the timeout map, or {@code null}
	 */
	@SuppressWarnings("unchecked")
	public Map<String, java.lang.Object> getTimeout() {
		java.lang.Object value = getDetailValue(getDetails(), "TimedOut");
		if (value instanceof Map) {
			java.lang.Object duration = ((Map<String, java.lang.Object>) value).get("duration");
			if (duration instanceof Map) {
				return (Map<String, java.lang.Object>) duration;
			}
		}
		return null;
	}
}
