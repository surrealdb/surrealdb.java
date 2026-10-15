package com.surrealdb;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import com.surrealdb.signin.RecordCredential;
import com.surrealdb.signin.RootCredential;
import com.surrealdb.signin.Token;

/**
 * set/unset, info, and refresh/revoke of access/refresh token pairs.
 */
public class SessionAuthTests {

	private static final String SERVER_URL = System.getenv().getOrDefault("SURREALDB_URL", "ws://localhost:8000");

	private static Map<String, java.lang.Object> credentials() {
		final Map<String, java.lang.Object> params = new HashMap<>();
		params.put("email", "a@b.com");
		params.put("pass", "secret");
		return params;
	}

	private static void defineRefreshableAccess(Surreal surreal) {
		// Record users may only read their own record (tables default to no access).
		surreal.query("DEFINE TABLE user PERMISSIONS FOR select WHERE id = $auth.id");
		surreal.query("DEFINE ACCESS user ON DATABASE TYPE RECORD "
				+ "SIGNUP (CREATE user SET email = $email, pass = crypto::argon2::generate($pass)) "
				+ "SIGNIN (SELECT * FROM user WHERE email = $email AND crypto::argon2::compare(pass, $pass)) "
				+ "WITH REFRESH DURATION FOR GRANT 15d, FOR TOKEN 1h, FOR SESSION 1h");
	}

	private static void withEmbedded(Consumer<Surreal> test) {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			test.accept(surreal);
		}
	}

	private static void withServer(Consumer<Surreal> test) {
		try (final Surreal surreal = new Surreal()) {
			try {
				surreal.connect(SERVER_URL);
			} catch (SurrealException e) {
				Assumptions.abort("No SurrealDB server at " + SERVER_URL);
			}
			surreal.signin(new RootCredential("root", "root"));
			surreal.useNs("test_ns").useDb("auth_" + System.nanoTime());
			test.accept(surreal);
		}
	}

	private static void setAndUnset(Surreal surreal) {
		surreal.set("greeting", "hello").set("limit", 3);
		assertEquals("hello", surreal.query("RETURN $greeting").take(0).getString());
		assertEquals(3L, surreal.query("RETURN $limit").take(0).getLong());
		surreal.unset("greeting");
		assertTrue(surreal.query("RETURN $greeting").take(0).isNone());
		assertEquals(3L, surreal.query("RETURN $limit").take(0).getLong());
	}

	@Test
	void setAndUnsetEmbedded() {
		withEmbedded(SessionAuthTests::setAndUnset);
	}

	@Test
	void setAndUnsetRemote() {
		withServer(SessionAuthTests::setAndUnset);
	}

	private static void refreshRevokeAndInfo(Surreal surreal) {
		defineRefreshableAccess(surreal);
		final String ns = surreal.getNamespace();
		final String db = surreal.getDatabase();
		// Not signed in as a record user: no info.
		assertFalse(surreal.info().isPresent());

		final Token signedUp = surreal.signup(new RecordCredential(ns, db, "user", credentials()));
		assertNotNull(signedUp.getRefresh(), "WITH REFRESH access must return a refresh token");

		final Optional<Value> info = surreal.info();
		assertTrue(info.isPresent());
		assertEquals("a@b.com", info.get().getObject().get("email").getString());

		// Refresh hands out a new pair and keeps the session authenticated.
		final Token refreshed = surreal.refresh(signedUp);
		assertNotNull(refreshed.getAccess());
		assertNotNull(refreshed.getRefresh());
		assertNotEquals(signedUp.getRefresh(), refreshed.getRefresh());
		assertTrue(surreal.info().isPresent());

		// A refresh token is single use, and a revoked one is rejected.
		assertThrows(SurrealException.class, () -> surreal.refresh(signedUp));
		surreal.revoke(refreshed);
		assertThrows(SurrealException.class, () -> surreal.refresh(refreshed));
	}

	@Test
	void refreshRevokeAndInfoEmbedded() {
		withEmbedded(SessionAuthTests::refreshRevokeAndInfo);
	}

	@Test
	void refreshRevokeAndInfoRemote() {
		withServer(SessionAuthTests::refreshRevokeAndInfo);
	}

	@Test
	void refreshNeedsARefreshToken() {
		withEmbedded(surreal -> {
			assertThrows(IllegalArgumentException.class, () -> surreal.refresh(new Token("access-only")));
			assertThrows(IllegalArgumentException.class, () -> surreal.revoke(new Token("access-only")));
		});
	}
}
