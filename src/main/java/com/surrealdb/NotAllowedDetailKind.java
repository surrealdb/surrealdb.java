package com.surrealdb;

/**
 * Detail kind constants for not-allowed errors.
 *
 * @see NotAllowedException
 */
public final class NotAllowedDetailKind {

	public static final String SCRIPTING = "Scripting";
	/** Auth sub-detail; inner variants are defined in {@link AuthDetailKind}. */
	public static final String AUTH = "Auth";
	public static final String METHOD = "Method";
	public static final String FUNCTION = "Function";
	public static final String TARGET = "Target";

	private NotAllowedDetailKind() {
	}
}
