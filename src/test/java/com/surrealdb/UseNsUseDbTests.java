package com.surrealdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Proves that use_ns must be called before use_db: calling use_db without a
 * prior use_ns causes "Cannot use database without namespace"; calling use_ns
 * then use_db succeeds. Also tests that useNs/useDb/useDefaults store the
 * server response and getNamespace/getDatabase return it.
 */
public class UseNsUseDbTests {

	@Test
	void getNamespace_getDatabase_nullAfterConnect() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory");
			assertNull(surreal.getNamespace());
			assertNull(surreal.getDatabase());
		}
	}

	@Test
	void getNamespace_getDatabase_nullAfterNewSessionBeforeUse() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			try (Surreal other = surreal.newSession()) {
				assertNull(other.getNamespace());
				assertNull(other.getDatabase());
			}
		}
	}

	@Test
	void useNs_returnsThis_forChaining() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory");
			Surreal result = surreal.useNs("test_ns");
			assertSame(surreal, result);
		}
	}

	@Test
	void useDb_returnsThis_forChaining() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns");
			Surreal result = surreal.useDb("test_db");
			assertSame(surreal, result);
		}
	}

	@Test
	void useDefaults_returnsThis_forChaining() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory");
			Surreal result = surreal.useDefaults();
			assertSame(surreal, result);
		}
	}

	@Test
	void useNsThenUseDb_succeeds() {
		try (Surreal surreal = new Surreal()) {
			// Proves that calling use_ns first, then use_db, works: no exception and we can
			// query.
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			Response response = surreal.query("INFO FOR DB");
			assertNotNull(response);
			assertTrue(response.size() >= 1);
		}
	}

	@Test
	void useDbWithoutUseNs_throws() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory");
			// Never call useNs — call useDb only
			SurrealException e = assertThrows(SurrealException.class, () -> {
				surreal.useDb("test_db");
			});
			assertNotNull(e.getMessage());
			assertTrue(e.getMessage().toLowerCase().contains("namespace"),
					"Expected message to mention namespace, got: " + e.getMessage());
		}
	}

	@Test
	void useNs_storesNamespaceAndDatabaseViaGetters() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory");
			surreal.useNs("test_ns");
			assertEquals("test_ns", surreal.getNamespace());
			// database may be null after use_ns only
		}
	}

	@Test
	void useDb_storesNamespaceAndDatabaseViaGetters() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns");
			surreal.useDb("test_db");
			assertEquals("test_ns", surreal.getNamespace());
			assertEquals("test_db", surreal.getDatabase());
		}
	}

	@Test
	void useDefaults_storesNamespaceAndDatabaseViaGetters() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory");
			surreal.useDefaults();
			// getNamespace() and getDatabase() return whatever the server sent (may be
			// null)
			surreal.getNamespace();
			surreal.getDatabase();
		}
	}
}
