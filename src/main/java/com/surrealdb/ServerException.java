package com.surrealdb;

import java.util.Map;

// Note: java.lang.Object is used explicitly throughout because
// com.surrealdb.Object shadows it in this package.

/**
 * Base class for all exceptions originating from the SurrealDB server.
 *
 * <p>Carries structured error information: a machine-readable {@link #getKind() kind},
 * optional {@link #getDetails() details} using the {@code {kind, details?}} wire format,
 * and an optional typed {@link #getServerCause() cause} chain.
 *
 * <p>Details follow the internally-tagged format where each detail object has a
 * {@code "kind"} field and an optional {@code "details"} field:
 * <ul>
 *   <li>Unit variant: {@code {"kind": "Parse"}}</li>
 *   <li>Newtype variant: {@code {"kind": "Auth", "details": {"kind": "TokenExpired"}}}</li>
 *   <li>Struct variant: {@code {"kind": "Table", "details": {"name": "users"}}}</li>
 * </ul>
 *
 * <p>For backward compatibility with older servers, the legacy externally-tagged
 * format ({@code "Parse"}, {@code {"Auth": "TokenExpired"}},
 * {@code {"Table": {"name": "users"}}}) is also supported by all detail helpers.
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
	 * <p>Details use the {@code {kind, details?}} wire format. The value is either:
	 * <ul>
	 *   <li>{@code null} -- no details</li>
	 *   <li>a {@code Map<String, Object>} with a {@code "kind"} key and optional
	 *       {@code "details"} key (new internally-tagged format)</li>
	 *   <li>a {@link String} -- a unit variant in the legacy format (e.g. {@code "Parse"})</li>
	 *   <li>a {@code Map<String, Object>} without {@code "kind"} -- legacy externally-tagged format</li>
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

	// ---- Detail navigation helpers ----
	//
	// SurrealDB v3 uses a recursive { "kind": "...", "details": ... } format
	// for error details (internally-tagged). Older servers used serde's
	// externally-tagged format ("Parse" / {"Auth": "TokenExpired"} / {"Table": {"name": "users"}}).
	//
	// All helpers support both formats for backward compatibility.

	/**
	 * Extracts the {@code "kind"} string from a {@code {kind, details?}} detail
	 * object. Returns {@code null} if the details are not in internally-tagged format.
	 *
	 * @param details the details object (may be {@code null})
	 * @return the kind string, or {@code null}
	 */
	@SuppressWarnings("unchecked")
	static String detailKind(java.lang.Object details) {
		if (details instanceof Map) {
			java.lang.Object k = ((Map<String, java.lang.Object>) details).get("kind");
			if (k instanceof String) {
				return (String) k;
			}
		}
		return null;
	}

	/**
	 * Extracts the {@code "details"} value from a {@code {kind, details?}} detail
	 * object. Returns {@code null} if not present or not in internally-tagged format.
	 *
	 * @param details the details object (may be {@code null})
	 * @return the inner details value, or {@code null}
	 */
	@SuppressWarnings("unchecked")
	static java.lang.Object detailInner(java.lang.Object details) {
		if (details instanceof Map) {
			return ((Map<String, java.lang.Object>) details).get("details");
		}
		return null;
	}

	/**
	 * Checks whether the details object matches a given variant key.
	 * Supports both new and old formats:
	 * <ul>
	 *   <li>New: {@code {"kind": "Parse"}} -- checks if {@code kind} equals key</li>
	 *   <li>Old: {@code "Parse"} -- checks if the string equals key</li>
	 *   <li>Old: {@code {"Parse": ...}} -- checks if the map contains key</li>
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
		// New format: {"kind": "Parse"}
		String dk = detailKind(details);
		if (dk != null) {
			return dk.equals(key);
		}
		// Old format: "Parse" (bare string)
		if (details instanceof String) {
			return ((String) details).equals(key);
		}
		// Old format: {"Parse": ...} (map key)
		if (details instanceof Map) {
			return ((Map<String, java.lang.Object>) details).containsKey(key);
		}
		return false;
	}

	/**
	 * Extracts the inner value for a given variant key.
	 * Supports both new and old formats:
	 * <ul>
	 *   <li>New: if {@code kind} equals key, returns {@code details["details"]}</li>
	 *   <li>Old: if details is a map, returns {@code details[key]}</li>
	 *   <li>Old: if details is a string matching key, returns {@code null} (unit variant)</li>
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
		// New format: {"kind": "Auth", "details": {"kind": "TokenExpired"}}
		String dk = detailKind(details);
		if (dk != null) {
			if (dk.equals(key)) {
				return detailInner(details);
			}
			return null;
		}
		// Old format: unit variant string has no value
		if (details instanceof String) {
			return null;
		}
		// Old format: {"Auth": "TokenExpired"} or {"Table": {"name": "users"}}
		if (details instanceof Map) {
			return ((Map<String, java.lang.Object>) details).get(key);
		}
		return null;
	}

	/**
	 * Extracts a string field from a variant's inner details.
	 * Supports both new and old formats.
	 *
	 * <p>New: {@code {"kind": "Table", "details": {"name": "users"}}}
	 * <br>Old: {@code {"Table": {"name": "users"}}}
	 * <br>Both: {@code detailField(details, "Table", "name")} returns {@code "users"}.
	 *
	 * @param details the details object
	 * @param key     the variant key
	 * @param field   the field name inside the inner object
	 * @return the string value, or {@code null}
	 */
	@SuppressWarnings("unchecked")
	static String detailField(java.lang.Object details, String key, String field) {
		java.lang.Object inner = getDetailValue(details, key);
		if (inner instanceof Map) {
			java.lang.Object value = ((Map<String, java.lang.Object>) inner).get(field);
			return value instanceof String ? (String) value : null;
		}
		return null;
	}

	/**
	 * Extracts a string from the inner details of a newtype variant.
	 * Supports both new and old formats:
	 * <ul>
	 *   <li>New: {@code {"kind": "Auth", "details": {"kind": "TokenExpired"}}}
	 *       -- returns {@code "TokenExpired"}</li>
	 *   <li>Old: {@code {"Auth": "TokenExpired"}} -- returns {@code "TokenExpired"}</li>
	 * </ul>
	 *
	 * @param details the details object
	 * @param key     the variant key
	 * @return the string value, or {@code null}
	 */
	static String getDetailString(java.lang.Object details, String key) {
		java.lang.Object inner = getDetailValue(details, key);
		if (inner == null) {
			return null;
		}
		// New format: inner is {"kind": "TokenExpired"}
		String dk = detailKind(inner);
		if (dk != null) {
			return dk;
		}
		// Old format: inner is "TokenExpired"
		if (inner instanceof String) {
			return (String) inner;
		}
		return null;
	}

	// ---- Legacy helpers (delegate to new ones for backward source compat) ----

	/**
	 * @deprecated Use {@link #detailField(Object, String, String)} instead.
	 */
	@SuppressWarnings("unchecked")
	static String getNestedString(java.lang.Object details, String outerKey, String innerKey) {
		return detailField(details, outerKey, innerKey);
	}

	/**
	 * @deprecated Use {@link #getDetailString(Object, String)} with an equality check instead.
	 */
	static boolean isNewtypeValue(java.lang.Object details, String outerKey, String value) {
		return value.equals(getDetailString(details, outerKey));
	}
}
