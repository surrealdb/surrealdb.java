package com.surrealdb;

/**
 * Detail kind constants for authentication errors.
 *
 * <p>These appear nested inside {@link NotAllowedDetailKind#AUTH} details.
 *
 * @see NotAllowedException
 */
public final class AuthDetailKind {

	public static final String TOKEN_EXPIRED = "TokenExpired";
	public static final String SESSION_EXPIRED = "SessionExpired";
	public static final String INVALID_AUTH = "InvalidAuth";
	public static final String UNEXPECTED_AUTH = "UnexpectedAuth";
	public static final String MISSING_USER_OR_PASS = "MissingUserOrPass";
	public static final String NO_SIGNIN_TARGET = "NoSigninTarget";
	public static final String INVALID_PASS = "InvalidPass";
	public static final String TOKEN_MAKING_FAILED = "TokenMakingFailed";
	public static final String INVALID_SIGNUP = "InvalidSignup";
	public static final String INVALID_ROLE = "InvalidRole";
	public static final String NOT_ALLOWED = "NotAllowed";

	private AuthDetailKind() {
	}
}
