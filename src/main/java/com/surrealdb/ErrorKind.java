package com.surrealdb;

/**
 * Machine-readable error kind constants returned by
 * {@link ServerException#getKind()}.
 *
 * <p>These values match the {@code ErrorKind} enum defined in the SurrealDB
 * server and are stable across SDK versions.
 */
public final class ErrorKind {

	/** Invalid input: parse error, invalid request or params. */
	public static final String VALIDATION = "Validation";

	/** Feature or config not supported (e.g. live queries, GraphQL). */
	public static final String CONFIGURATION = "Configuration";

	/** User-thrown error (THROW in SurrealQL). */
	public static final String THROWN = "Thrown";

	/** Query execution failure (timeout, cancelled, not executed). */
	public static final String QUERY = "Query";

	/** Serialization or deserialization error. */
	public static final String SERIALIZATION = "Serialization";

	/** Permission denied, method not allowed, function or scripting blocked. */
	public static final String NOT_ALLOWED = "NotAllowed";

	/** Resource not found (table, record, namespace, RPC method). */
	public static final String NOT_FOUND = "NotFound";

	/** Duplicate resource (table, record, namespace). */
	public static final String ALREADY_EXISTS = "AlreadyExists";

	/** Client connection error (SDK-side only). */
	public static final String CONNECTION = "Connection";

	/** Unexpected or unknown error (fallback for unrecognised kinds). */
	public static final String INTERNAL = "Internal";

	private ErrorKind() {
	}
}
