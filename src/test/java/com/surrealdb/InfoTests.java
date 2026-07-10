package com.surrealdb;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.surrealdb.pojos.User;
import com.surrealdb.signin.RecordCredential;
import com.surrealdb.signin.Token;

/**
 * Tests for {@link Surreal#info()} and {@link Surreal#info(Class)}: fetching
 * the currently-authenticated record user ({@code SELECT * FROM $auth}).
 */
public class InfoTests {

	/** Java 8 compatible: Map.of("email", "a@b.com", "pass", "p") */
	private static Map<String, String> params(String k1, String v1, String k2, String v2) {
		Map<String, String> m = new HashMap<>();
		m.put(k1, v1);
		m.put(k2, v2);
		return m;
	}

	/**
	 * Defines a `user` table (readable by its own record user) with a record
	 * access, then signs up a record user with the given email.
	 */
	private static Token signupUser(Surreal surreal, String email) {
		surreal.query("DEFINE TABLE user SCHEMALESS PERMISSIONS FOR select WHERE id = $auth.id");
		surreal.query("DEFINE ACCESS user ON DATABASE TYPE RECORD "
				+ "SIGNUP ( CREATE user SET email = $email, pass = crypto::argon2::generate($pass) ) "
				+ "SIGNIN ( SELECT * FROM user WHERE email = $email AND crypto::argon2::compare(pass, $pass) ) "
				+ "DURATION FOR TOKEN 15m, FOR SESSION 12h");
		return surreal.signup(new RecordCredential("user", params("email", email, "pass", "p")));
	}

	@Test
	void info_afterSignup_returnsAuthenticatedRecord() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			Token token = signupUser(surreal, "tobie@example.com");
			assertNotNull(token.getAccess());
			// info() returns the record user pointed to by $auth
			Optional<Value> info = surreal.info();
			assertTrue(info.isPresent());
			Value value = info.get();
			assertTrue(value.isObject());
			assertEquals("tobie@example.com", value.getObject().get("email").getString());
			// The returned record is the $auth record
			RecordId auth = surreal.query("RETURN $auth").take(0).getRecordId();
			assertEquals(auth, value.getObject().get("id").getRecordId());
		}
	}

	@Test
	void info_typed_hydratesPojo() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			signupUser(surreal, "jaime@example.com");
			Optional<User> user = surreal.info(User.class);
			assertTrue(user.isPresent());
			assertEquals("jaime@example.com", user.get().email);
			assertNotNull(user.get().id);
			assertEquals("user", user.get().id.getTable());
		}
	}

	@Test
	void info_unauthenticated_returnsEmpty() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			assertFalse(surreal.info().isPresent());
			assertFalse(surreal.info(User.class).isPresent());
		}
	}

	@Test
	void info_afterInvalidate_returnsEmpty() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			signupUser(surreal, "emmanuel@example.com");
			assertTrue(surreal.info().isPresent());
			surreal.invalidate();
			assertFalse(surreal.info().isPresent());
		}
	}
}
