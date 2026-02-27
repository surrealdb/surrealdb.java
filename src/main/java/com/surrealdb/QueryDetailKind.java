package com.surrealdb;

/**
 * Detail kind constants for query errors.
 *
 * @see QueryException
 */
public final class QueryDetailKind {

	public static final String NOT_EXECUTED = "NotExecuted";
	public static final String TIMED_OUT = "TimedOut";
	public static final String CANCELLED = "Cancelled";

	private QueryDetailKind() {
	}
}
