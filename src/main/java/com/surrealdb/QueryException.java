package com.surrealdb;

import java.util.Map;

/**
 * Query execution failure (not executed, timed out, cancelled).
 *
 * <p>Details use the {@code {kind, details?}} format with variants defined
 * in {@link QueryDetailKind}.
 *
 * @see ErrorKind#QUERY
 */
public class QueryException extends ServerException {

	QueryException(String message, java.lang.Object details, ServerException cause) {
		super(ErrorKind.QUERY, message, details, cause);
	}

	/**
	 * Returns {@code true} when the query was not executed
	 * (e.g. a previous statement in a multi-statement query failed).
	 *
	 * @return whether the detail kind is {@code NotExecuted}
	 */
	public boolean isNotExecuted() {
		return hasDetailKey(getDetails(), QueryDetailKind.NOT_EXECUTED);
	}

	/**
	 * Returns {@code true} when the query timed out.
	 *
	 * @return whether the detail kind is {@code TimedOut}
	 */
	public boolean isTimedOut() {
		return hasDetailKey(getDetails(), QueryDetailKind.TIMED_OUT);
	}

	/**
	 * Returns {@code true} when the query was cancelled.
	 *
	 * @return whether the detail kind is {@code Cancelled}
	 */
	public boolean isCancelled() {
		return hasDetailKey(getDetails(), QueryDetailKind.CANCELLED);
	}

	/**
	 * Returns the timeout duration when the query timed out, as a map
	 * containing {@code "secs"} and {@code "nanos"} keys.
	 *
	 * @return the timeout map, or {@code null}
	 */
	@SuppressWarnings("unchecked")
	public Map<String, java.lang.Object> getTimeout() {
		java.lang.Object inner = getDetailValue(getDetails(), QueryDetailKind.TIMED_OUT);
		if (inner instanceof Map) {
			java.lang.Object duration = ((Map<String, java.lang.Object>) inner).get("duration");
			if (duration instanceof Map) {
				return (Map<String, java.lang.Object>) duration;
			}
		}
		return null;
	}
}
