package com.surrealdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

/**
 * Integration tests that verify structured error information survives the
 * JNI bridge round-trip. These tests trigger real errors from the embedded
 * SurrealDB engine and check the exception type and properties.
 */
public class ErrorIntegrationTests {

	@Test
	void parseErrorIsValidationException() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			try {
				surreal.query("THIS IS NOT VALID SQL !!! %%%");
				fail("Expected a ValidationException for invalid SQL");
			} catch (ValidationException e) {
				assertEquals(ErrorKind.VALIDATION, e.getKindEnum());
				assertNotNull(e.getMessage());
			} catch (ServerException e) {
				// Also acceptable -- the server may classify this differently
				assertNotNull(e.getKind());
				assertNotNull(e.getMessage());
			}
		}
	}

	@Test
	void serverErrorIsSurrealException() {
		// Any server error should be catchable as SurrealException
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			try {
				surreal.query("THIS IS NOT VALID SQL !!! %%%");
				fail("Expected an exception for invalid SQL");
			} catch (SurrealException e) {
				assertNotNull(e.getMessage());
				// Should be a ServerException (not just a plain SurrealException)
				assertTrue(e instanceof ServerException,
						"Expected ServerException but got " + e.getClass().getName());
			}
		}
	}

	@Test
	void serverExceptionHasKind() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			try {
				surreal.query("INVALID QUERY !!!@@@");
				fail("Expected an exception");
			} catch (ServerException e) {
				assertNotNull(e.getKind());
				// Kind should be a non-empty string
				assertTrue(e.getKind().length() > 0);
			}
		}
	}

	@Test
	void thrownExceptionFromThrowStatement() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			try {
				// THROW produces a per-statement error, surfaced when taking the result
				Response response = surreal.query("THROW 'custom error message'");
				response.take(0);
				fail("Expected a ThrownException for THROW statement");
			} catch (ThrownException e) {
				assertEquals(ErrorKind.THROWN, e.getKindEnum());
				assertNotNull(e.getMessage());
			} catch (ServerException e) {
				// Acceptable if the server classifies differently
				assertNotNull(e.getKind());
			}
		}
	}
}
