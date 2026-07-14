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
 * Tests for {@link Surreal#auth()} and {@link Surreal#auth(Class)}: fetching
 * the currently-authenticated record user ({@code SELECT * FROM $auth}).
 */
public class AuthTests {

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
		return signupUser(surreal, email, "WHERE id = $auth.id");
	}

	/**
	 * Defines a `user` table with the given select permission clause and a record
	 * access, then signs up a record user with the given email.
	 */
	private static Token signupUser(Surreal surreal, String email, String selectPermission) {
		surreal.query("DEFINE TABLE user SCHEMALESS PERMISSIONS FOR select " + selectPermission);
		surreal.query("DEFINE ACCESS user ON DATABASE TYPE RECORD "
				+ "SIGNUP ( CREATE user SET email = $email, pass = crypto::argon2::generate($pass) ) "
				+ "SIGNIN ( SELECT * FROM user WHERE email = $email AND crypto::argon2::compare(pass, $pass) ) "
				+ "DURATION FOR TOKEN 15m, FOR SESSION 12h");
		return surreal.signup(new RecordCredential("user", params("email", email, "pass", "p")));
	}

	@Test
	void auth_afterSignup_returnsAuthenticatedRecord() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			Token token = signupUser(surreal, "tobie@example.com");
			assertNotNull(token.getAccess());
			// auth() returns the record user pointed to by $auth
			Optional<Value> auth = surreal.auth();
			assertTrue(auth.isPresent());
			Value value = auth.get();
			assertTrue(value.isObject());
			assertEquals("tobie@example.com", value.getObject().get("email").getString());
			// The returned record is the $auth record
			RecordId authId = surreal.query("RETURN $auth").take(0).getRecordId();
			assertEquals(authId, value.getObject().get("id").getRecordId());
		}
	}

	@Test
	void auth_typed_hydratesPojo() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			signupUser(surreal, "jaime@example.com");
			Optional<User> user = surreal.auth(User.class);
			assertTrue(user.isPresent());
			assertEquals("jaime@example.com", user.get().email);
			assertNotNull(user.get().id);
			assertEquals("user", user.get().id.getTable());
		}
	}

	@Test
	void auth_unauthenticated_returnsEmpty() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			assertFalse(surreal.auth().isPresent());
			assertFalse(surreal.auth(User.class).isPresent());
		}
	}

	@Test
	void auth_recordNotVisibleToUser_returnsEmpty() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			// The table's select permission excludes the record user's own record
			Token token = signupUser(surreal, "denied@example.com", "NONE");
			assertNotNull(token.getAccess());
			// The session is authenticated: $auth points to the record user...
			RecordId authId = surreal.query("RETURN $auth").take(0).getRecordId();
			assertEquals("user", authId.getTable());
			// ...but the record is not visible to it, so auth() is empty
			assertFalse(surreal.auth().isPresent());
			assertFalse(surreal.auth(User.class).isPresent());
		}
	}

	@Test
	void auth_afterInvalidate_returnsEmpty() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			signupUser(surreal, "emmanuel@example.com");
			assertTrue(surreal.auth().isPresent());
			surreal.invalidate();
			assertFalse(surreal.auth().isPresent());
		}
	}
}
