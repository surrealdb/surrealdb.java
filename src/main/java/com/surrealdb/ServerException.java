package com.surrealdb;

import java.util.Map;

// Note: java.lang.Object is used explicitly throughout because
// com.surrealdb.Object shadows it in this package.

/**
 * Base class for all exceptions originating from the SurrealDB server.
 *
 * <p>Carries structured error information: a machine-readable {@link #getKind() kind},
 * optional {@link #getDetails() details} (serde externally-tagged enum format), and
 * an optional typed {@link #getServerCause() cause} chain.
 *
 * <p>Specific error kinds are represented by subclasses (e.g. {@link NotAllowedException},
 * {@link NotFoundException}). When the server returns an unknown kind, a plain
 * {@code ServerException} is used (not {@link InternalException}) to preserve forward
 * compatibility.
 *
 * @see ErrorKind
 */
public class ServerException extends SurrealException {

	private final String kind;
	private final java.lang.Object details;
	private final ServerException serverCause;

	ServerException(String kind, String message, java.lang.Object details, ServerException cause) {
		super(message, cause);
		this.kind = kind;
		this.details = details;
		this.serverCause = cause;
	}

	/**
	 * Constructs a {@code ServerException} from a JSON details string.
	 * Used by the JNI bridge.
	 */
	ServerException(String kind, String message, String detailsJson, ServerException cause) {
		this(kind, message, DetailParser.parseDetailsJson(detailsJson), cause);
	}

	/**
	 * Returns the machine-readable error kind (e.g. {@code "NotAllowed"}).
	 *
	 * @return the error kind string, never {@code null}
	 * @see ErrorKind
	 */
	public String getKind() {
		return kind;
	}

	/**
	 * Returns the optional structured details for this error.
	 *
	 * <p>Details use serde's externally-tagged enum format. The value is either:
	 * <ul>
	 *   <li>{@code null} -- no details</li>
	 *   <li>a {@link String} -- a unit variant (e.g. {@code "Parse"})</li>
	 *   <li>a {@code Map<String, Object>} -- a struct or newtype variant
	 *       (e.g. {@code {"Table": {"name": "users"}}})</li>
	 * </ul>
	 *
	 * <p>Prefer the typed convenience getters on subclasses (e.g.
	 * {@link NotFoundException#getTableName()}) over inspecting details directly.
	 *
	 * @return the details object, or {@code null}
	 */
	public java.lang.Object getDetails() {
		return details;
	}

	/**
	 * Returns the typed server-side cause of this error, if any.
	 *
	 * <p>This is equivalent to calling {@link #getCause()} and casting to
	 * {@code ServerException}, but avoids the cast.
	 *
	 * @return the server cause, or {@code null}
	 */
	public ServerException getServerCause() {
		return serverCause;
	}

	/**
	 * Checks whether this error or any error in its cause chain has the given
	 * {@link ErrorKind kind}.
	 *
	 * @param kind the kind to look for
	 * @return {@code true} if any error in the chain matches
	 */
	public boolean hasKind(String kind) {
		return findCause(kind) != null;
	}

	/**
	 * Finds the first error in the cause chain (including this error) that has
	 * the given {@link ErrorKind kind}.
	 *
	 * @param kind the kind to look for
	 * @return the matching {@code ServerException}, or {@code null}
	 */
	public ServerException findCause(String kind) {
		ServerException current = this;
		while (current != null) {
			if (kind.equals(current.kind)) {
				return current;
			}
			current = current.serverCause;
		}
		return null;
	}

	// ---- Detail navigation helpers (serde externally-tagged enum format) ----

	/**
	 * Checks whether the details object contains a given key.
	 *
	 * <p>Handles the serde externally-tagged format:
	 * <ul>
	 *   <li>If details is a {@code String}, returns {@code true} when the string
	 *       equals the key.</li>
	 *   <li>If details is a {@code Map}, returns {@code true} when the map
	 *       contains the key.</li>
	 * </ul>
	 *
	 * @param details the details object (may be {@code null})
	 * @param key     the key to look for
	 * @return {@code true} if the key is present
	 */
	@SuppressWarnings("unchecked")
	static boolean hasDetailKey(java.lang.Object details, String key) {
		if (details == null) {
			return false;
		}
		if (details instanceof String) {
			return ((String) details).equals(key);
		}
		if (details instanceof Map) {
			return ((Map<String, java.lang.Object>) details).containsKey(key);
		}
		return false;
	}

	/**
	 * Extracts the value for a key from the details object.
	 *
	 * <p>Handles the serde externally-tagged format:
	 * <ul>
	 *   <li>If details is a {@code String} that equals the key, returns
	 *       {@code null} (unit variant, key is present but has no value).</li>
	 *   <li>If details is a {@code Map} and contains the key, returns the
	 *       mapped value.</li>
	 * </ul>
	 *
	 * @param details the details object (may be {@code null})
	 * @param key     the key to extract
	 * @return the value, or {@code null}
	 */
	@SuppressWarnings("unchecked")
	static java.lang.Object getDetailValue(java.lang.Object details, String key) {
		if (details == null) {
			return null;
		}
		if (details instanceof String) {
			return null;
		}
		if (details instanceof Map) {
			return ((Map<String, java.lang.Object>) details).get(key);
		}
		return null;
	}

	/**
	 * Extracts a string field from a nested struct variant.
	 *
	 * <p>For details like {@code {"Table": {"name": "users"}}}, calling
	 * {@code getNestedString(details, "Table", "name")} returns {@code "users"}.
	 *
	 * @param details  the details object
	 * @param outerKey the outer key (variant name)
	 * @param innerKey the inner key (field name)
	 * @return the string value, or {@code null}
	 */
	@SuppressWarnings("unchecked")
	static String getNestedString(java.lang.Object details, String outerKey, String innerKey) {
		java.lang.Object outer = getDetailValue(details, outerKey);
		if (outer instanceof Map) {
			java.lang.Object inner = ((Map<String, java.lang.Object>) outer).get(innerKey);
			return inner instanceof String ? (String) inner : null;
		}
		return null;
	}

	/**
	 * Checks whether a newtype variant wraps a specific string value.
	 *
	 * <p>For details like {@code {"Auth": "TokenExpired"}}, calling
	 * {@code isNewtypeValue(details, "Auth", "TokenExpired")} returns {@code true}.
	 *
	 * @param details  the details object
	 * @param outerKey the outer key (variant name)
	 * @param value    the expected string value
	 * @return {@code true} if the match is found
	 */
	static boolean isNewtypeValue(java.lang.Object details, String outerKey, String value) {
		java.lang.Object outer = getDetailValue(details, outerKey);
		return value.equals(outer);
	}
}
