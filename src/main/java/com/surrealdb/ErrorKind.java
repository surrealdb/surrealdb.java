package com.surrealdb;

import java.util.HashMap;
import java.util.Map;

/**
 * Machine-readable error kind, aligned with the SurrealDB Rust SDK's {@code ErrorDetails} enum.
 *
 * <p>Returned by {@link ServerException#getKindEnum()}. Use this for type-safe matching instead
 * of {@link ServerException#getKind()} when the kind is known. For unknown (future) kinds,
 * {@link #UNKNOWN} is used and the raw string is available via {@link ServerException#getKind()}.
 */
public enum ErrorKind {

	VALIDATION("Validation"),
	CONFIGURATION("Configuration"),
	THROWN("Thrown"),
	QUERY("Query"),
	SERIALIZATION("Serialization"),
	NOT_ALLOWED("NotAllowed"),
	NOT_FOUND("NotFound"),
	ALREADY_EXISTS("AlreadyExists"),
	CONNECTION("Connection"),
	INTERNAL("Internal"),
	/** Unknown kind from a newer server; raw string is in {@link ServerException#getKind()}. */
	UNKNOWN(null);

	private static final Map<String, ErrorKind> LOOKUP;

	static {
		LOOKUP = new HashMap<>();
		for (ErrorKind k : values()) {
			if (k.raw != null) {
				LOOKUP.put(k.raw, k);
			}
		}
	}

	private final String raw;

	ErrorKind(String raw) {
		this.raw = raw;
	}

	/**
	 * Resolves a kind string from the wire to an enum constant.
	 * Unknown strings return {@link #UNKNOWN}.
	 *
	 * @param kind the kind string (e.g. from the server)
	 * @return the matching enum constant, or {@link #UNKNOWN}
	 */
	public static ErrorKind fromString(String kind) {
		if (kind == null) {
			return UNKNOWN;
		}
		return LOOKUP.getOrDefault(kind, UNKNOWN);
	}

	/**
	 * Returns the wire string for this kind, or {@code null} for {@link #UNKNOWN}.
	 */
	public String getRaw() {
		return raw;
	}
}
