package com.surrealdb;

/**
 * Detail kind constants for serialization errors.
 *
 * @see SerializationException
 */
public final class SerializationDetailKind {

	public static final String SERIALIZATION = "Serialization";
	public static final String DESERIALIZATION = "Deserialization";

	private SerializationDetailKind() {
	}
}
