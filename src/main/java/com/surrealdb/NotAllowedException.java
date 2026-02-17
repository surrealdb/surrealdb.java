package com.surrealdb;

/**
 * Permission denied, method not allowed, function or scripting blocked.
 *
 * @see ErrorKind#NOT_ALLOWED
 */
public class NotAllowedException extends ServerException {

	NotAllowedException(String message, java.lang.Object details, ServerException cause) {
		super(ErrorKind.NOT_ALLOWED, message, details, cause);
	}

	NotAllowedException(String message, String detailsJson, ServerException cause) {
		super(ErrorKind.NOT_ALLOWED, message, detailsJson, cause);
	}

	/**
	 * Returns {@code true} when the token used for authentication has expired.
	 *
	 * @return whether the detail matches {@code {"Auth": "TokenExpired"}}
	 */
	public boolean isTokenExpired() {
		return isNewtypeValue(getDetails(), "Auth", "TokenExpired");
	}

	/**
	 * Returns {@code true} when authentication failed (invalid credentials).
	 *
	 * @return whether the detail matches {@code {"Auth": "InvalidAuth"}}
	 */
	public boolean isInvalidAuth() {
		return isNewtypeValue(getDetails(), "Auth", "InvalidAuth");
	}

	/**
	 * Returns {@code true} when scripting is blocked.
	 *
	 * @return whether the detail is {@code "Scripting"}
	 */
	public boolean isScriptingBlocked() {
		return hasDetailKey(getDetails(), "Scripting");
	}

	/**
	 * Returns the name of the method that is not allowed, if applicable.
	 *
	 * @return the method name, or {@code null}
	 */
	public String getMethodName() {
		return getNestedString(getDetails(), "Method", "name");
	}

	/**
	 * Returns the name of the function that is not allowed, if applicable.
	 *
	 * @return the function name, or {@code null}
	 */
	public String getFunctionName() {
		return getNestedString(getDetails(), "Function", "name");
	}
}
