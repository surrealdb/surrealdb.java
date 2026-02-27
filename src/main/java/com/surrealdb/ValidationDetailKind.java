package com.surrealdb;

/**
 * Detail kind constants for validation errors.
 *
 * @see ValidationException
 */
public final class ValidationDetailKind {

	public static final String PARSE = "Parse";
	public static final String INVALID_REQUEST = "InvalidRequest";
	public static final String INVALID_PARAMS = "InvalidParams";
	public static final String NAMESPACE_EMPTY = "NamespaceEmpty";
	public static final String DATABASE_EMPTY = "DatabaseEmpty";
	public static final String INVALID_PARAMETER = "InvalidParameter";
	public static final String INVALID_CONTENT = "InvalidContent";
	public static final String INVALID_MERGE = "InvalidMerge";

	private ValidationDetailKind() {
	}
}
