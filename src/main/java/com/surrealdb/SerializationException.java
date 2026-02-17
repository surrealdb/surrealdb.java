package com.surrealdb;

/**
 * Serialization or deserialization error.
 *
 * @see ErrorKind#SERIALIZATION
 */
public class SerializationException extends ServerException {

	SerializationException(String message, java.lang.Object details, ServerException cause) {
		super(ErrorKind.SERIALIZATION, message, details, cause);
	}

	SerializationException(String message, String detailsJson, ServerException cause) {
		super(ErrorKind.SERIALIZATION, message, detailsJson, cause);
	}

	/**
	 * Returns {@code true} when this is specifically a deserialization error
	 * (as opposed to serialization).
	 *
	 * @return whether the detail is {@code "Deserialization"}
	 */
	public boolean isDeserialization() {
		return hasDetailKey(getDetails(), "Deserialization");
	}
}
