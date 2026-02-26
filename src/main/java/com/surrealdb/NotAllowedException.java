package com.surrealdb;

/**
 * Permission denied, method not allowed, function or scripting blocked.
 *
 * <p>Details use the {@code {kind, details?}} format with variants defined
 * in {@link NotAllowedDetailKind}. Auth details further nest
 * {@link AuthDetailKind} variants.
 *
 * @see ErrorKind#NOT_ALLOWED
 */
public class NotAllowedException extends ServerException {

	NotAllowedException(String message, java.lang.Object details, ServerException cause) {
		super(ErrorKind.NOT_ALLOWED, message, details, cause);
	}

	/**
	 * Returns {@code true} when the token used for authentication has expired.
	 *
	 * @return whether the detail matches {@code Auth -> TokenExpired}
	 */
	public boolean isTokenExpired() {
		return AuthDetailKind.TOKEN_EXPIRED.equals(getDetailString(getDetailValue(getDetails(), NotAllowedDetailKind.AUTH)));
	}

	/**
	 * Returns {@code true} when authentication failed (invalid credentials).
	 *
	 * @return whether the detail matches {@code Auth -> InvalidAuth}
	 */
	public boolean isInvalidAuth() {
		return AuthDetailKind.INVALID_AUTH.equals(getDetailString(getDetailValue(getDetails(), NotAllowedDetailKind.AUTH)));
	}

	/**
	 * Returns {@code true} when scripting is blocked.
	 *
	 * @return whether the detail kind is {@code Scripting}
	 */
	public boolean isScriptingBlocked() {
		return hasDetailKey(getDetails(), NotAllowedDetailKind.SCRIPTING);
	}

	/**
	 * Returns the name of the method that is not allowed, if applicable.
	 *
	 * @return the method name, or {@code null}
	 */
	public String getMethodName() {
		return detailField(getDetails(), NotAllowedDetailKind.METHOD, "name");
	}

	/**
	 * Returns the name of the function that is not allowed, if applicable.
	 *
	 * @return the function name, or {@code null}
	 */
	public String getFunctionName() {
		return detailField(getDetails(), NotAllowedDetailKind.FUNCTION, "name");
	}

	/**
	 * Returns the name of the target that is not allowed, if applicable.
	 *
	 * @return the target name, or {@code null}
	 */
	public String getTargetName() {
		return detailField(getDetails(), NotAllowedDetailKind.TARGET, "name");
	}

	// ---- Auth detail helpers ----

	private static String getDetailString(java.lang.Object inner) {
		if (inner == null) {
			return null;
		}
		String dk = detailKind(inner);
		if (dk != null) {
			return dk;
		}
		if (inner instanceof String) {
			return (String) inner;
		}
		return null;
	}
}
