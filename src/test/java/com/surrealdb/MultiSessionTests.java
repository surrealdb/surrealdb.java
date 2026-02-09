package com.surrealdb;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.surrealdb.pojos.Person;

/**
 * Tests for multi-session support: {@link Surreal#newSession()} creates a new
 * session that shares the connection but has independent namespace, database,
 * and auth state.
 */
public class MultiSessionTests {

	@Test
	void newSession_returnsDifferentInstance() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			Surreal other = surreal.newSession();
			assertNotSame(surreal, other);
		}
	}

	@Test
	void newSession_hasIndependentNamespaceAndDatabase() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("ns_a").useDb("db_a");
			try (Surreal other = surreal.newSession()) {
				assertNull(other.getNamespace());
				assertNull(other.getDatabase());
				other.useNs("ns_b").useDb("db_b");
				assertEquals("ns_b", other.getNamespace());
				assertEquals("db_b", other.getDatabase());
				assertEquals("ns_a", surreal.getNamespace());
				assertEquals("db_a", surreal.getDatabase());
			}
		}
	}

	@Test
	void newSession_sharesConnection_sameDataVisible() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			surreal.create(Person.class, "person", Helpers.tobie);
			try (Surreal other = surreal.newSession()) {
				other.useNs("test_ns").useDb("test_db");
				Response response = other.query("SELECT * FROM person");
				assertNotNull(response);
				assertTrue(response.size() >= 1);
			}
		}
	}

	@Test
	void newSession_canMutateAndOtherSeesIt() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			try (Surreal other = surreal.newSession()) {
				other.useNs("test_ns").useDb("test_db");
				other.query("CREATE person SET name = 'FromSessionB'");
				Response response = surreal.query("SELECT * FROM person WHERE name = 'FromSessionB'");
				assertNotNull(response);
				assertTrue(response.size() >= 1);
			}
		}
	}

	@Test
	void multipleNewSessions_allIndependent() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			try (Surreal s1 = surreal.newSession(); Surreal s2 = surreal.newSession()) {
				s1.useNs("n1").useDb("d1");
				s2.useNs("n2").useDb("d2");
				assertEquals("n1", s1.getNamespace());
				assertEquals("d1", s1.getDatabase());
				assertEquals("n2", s2.getNamespace());
				assertEquals("d2", s2.getDatabase());
				assertEquals("test_ns", surreal.getNamespace());
				assertEquals("test_db", surreal.getDatabase());
			}
		}
	}
}
