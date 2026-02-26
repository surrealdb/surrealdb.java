package com.surrealdb;

/**
 * Serialization or deserialization error.
 *
 * <p>Details use the {@code {kind, details?}} format with variants defined
 * in {@link SerializationDetailKind}.
 *
 * @see ErrorKind#SERIALIZATION
 */
public class SerializationException extends ServerException {

	SerializationException(String message, java.lang.Object details, ServerException cause) {
		super(ErrorKind.SERIALIZATION, message, details, cause);
	}

	/**
	 * Returns {@code true} when this is specifically a deserialization error
	 * (as opposed to serialization).
	 *
	 * @return whether the detail kind is {@code Deserialization}
	 */
	public boolean isDeserialization() {
		return hasDetailKey(getDetails(), SerializationDetailKind.DESERIALIZATION);
	}
}
