package com.surrealdb;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import com.surrealdb.signin.RecordCredential;
import com.surrealdb.signin.Token;

/**
 * Regression tests for the namespace/database the Java side reports. They used
 * to come from a cache filled by useNs()/useDb(), which went stale for forked
 * sessions and made record signin on a new session fail its "requires namespace
 * and database" guard.
 */
public class SessionNsDbTests {

	private static final String SERVER_URL = System.getenv().getOrDefault("SURREALDB_URL", "ws://localhost:8000");

	private static Map<String, java.lang.Object> credentials() {
		final Map<String, java.lang.Object> params = new HashMap<>();
		params.put("email", "a@b.com");
		params.put("pass", "secret");
		return params;
	}

	private static void defineUserAccess(Surreal surreal) {
		surreal.query("DEFINE ACCESS user ON DATABASE TYPE RECORD "
				+ "SIGNUP (CREATE user SET email = $email, pass = crypto::argon2::generate($pass)) "
				+ "SIGNIN (SELECT * FROM user WHERE email = $email AND crypto::argon2::compare(pass, $pass)) "
				+ "DURATION FOR TOKEN 1h, FOR SESSION 1h");
	}

	private static void recordSigninOnNewSession(Surreal surreal, String db) {
		surreal.useNs("test_ns").useDb(db);
		defineUserAccess(surreal);
		surreal.signup(new RecordCredential("test_ns", db, "user", credentials()));
		try (Surreal session = surreal.newSession()) {
			// The new session inherits test_ns/db from its parent...
			assertEquals("test_ns", session.getNamespace());
			assertEquals(db, session.getDatabase());
			// ...so a RecordCredential without ns/db resolves them from the session.
			final Token token = session.signin(new RecordCredential("user", credentials()));
			assertNotNull(token.getAccess());
		}
	}

	@Test
	void recordSigninOnNewSessionEmbedded() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory");
			recordSigninOnNewSession(surreal, "test_db");
		}
	}

	@Test
	void recordSigninOnNewSessionRemote() {
		try (final Surreal surreal = new Surreal()) {
			try {
				surreal.connect(SERVER_URL);
			} catch (SurrealException e) {
				Assumptions.abort("No SurrealDB server at " + SERVER_URL);
			}
			recordSigninOnNewSession(surreal, "nsdb_" + System.nanoTime());
		}
	}

	@Test
	void notConnectedReportsNull() {
		try (final Surreal surreal = new Surreal()) {
			assertNull(surreal.getNamespace());
			assertNull(surreal.getDatabase());
		}
	}
}
