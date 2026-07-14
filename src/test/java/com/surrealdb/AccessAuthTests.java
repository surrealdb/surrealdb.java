package com.surrealdb;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.surrealdb.signin.AccessCredential;
import com.surrealdb.signin.AccessKeyCredential;
import com.surrealdb.signin.RecordCredential;
import com.surrealdb.signin.Token;

/**
 * Tests for the authentication-parity additions alongside {@code auth()}:
 * {@link AccessKeyCredential} (signin with a {@code TYPE BEARER} access grant
 * key), {@link AccessCredential} (signin via a named system-level
 * {@code ACCESS} method with username/password), and
 * {@link Surreal#authenticate(Token)} (plain re-authenticate vs. a real
 * refresh-token rotation). Memory engine, patterned after
 * {@code CredentialTests}.
 */
public class AccessAuthTests {

	/** Java 8 compatible: Map.of("email", "a@b.com", "pass", "p") */
	private static Map<String, String> params(String k1, String v1, String k2, String v2) {
		Map<String, String> m = new HashMap<>();
		m.put(k1, v1);
		m.put(k2, v2);
		return m;
	}

	/**
	 * Extracts the bearer grant key from the result of
	 * {@code ACCESS ... GRANT FOR USER ...}, i.e. {@code result.grant.key}.
	 */
	private static String grantKey(Value grantResult) {
		return grantResult.getObject().get("grant").getObject().get("key").getString();
	}

	// -----------------------------------------------------------------
	// Gap 1: AccessKeyCredential (signin with a TYPE BEARER grant key)
	// -----------------------------------------------------------------

	@Test
	void signin_withAccessKey_onDatabaseBearerGrant_succeeds() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			surreal.query("DEFINE USER svcacct ON DATABASE PASSWORD 'pass' ROLES OWNER");
			surreal.query(
					"DEFINE ACCESS api_key ON DATABASE TYPE BEARER FOR USER DURATION FOR TOKEN 15m, FOR SESSION 12h");
			String key = grantKey(surreal.query("ACCESS api_key ON DATABASE GRANT FOR USER svcacct").take(0));
			assertNotNull(key);

			try (Surreal other = surreal.newSession()) {
				Token token = other.signin(new AccessKeyCredential("test_ns", "test_db", "api_key", key));
				assertNotNull(token);
				assertNotNull(token.getAccess());
				// The session is authenticated via the named access method.
				Value access = other.query("RETURN $access").take(0);
				assertEquals("api_key", access.getString());
			}
		}
	}

	@Test
	void signin_withAccessKey_databaseOmitted_onNamespaceBearerGrant_succeeds() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			surreal.query("DEFINE USER ns_svcacct ON NAMESPACE PASSWORD 'pass' ROLES OWNER");
			surreal.query(
					"DEFINE ACCESS ns_api_key ON NAMESPACE TYPE BEARER FOR USER DURATION FOR TOKEN 15m, FOR SESSION 12h");
			String key = grantKey(surreal.query("ACCESS ns_api_key ON NAMESPACE GRANT FOR USER ns_svcacct").take(0));
			assertNotNull(key);

			try (Surreal other = surreal.newSession()) {
				// database omitted (null): the credential is namespace-level only.
				Token token = other.signin(new AccessKeyCredential("test_ns", null, "ns_api_key", key));
				assertNotNull(token);
				assertNotNull(token.getAccess());
			}
		}
	}

	@Test
	void signin_withAccessKey_omittedNsDb_doesNotFallBackToSession_throws() {
		try (Surreal surreal = new Surreal()) {
			// useNs/useDb ARE called, but AccessKeyCredential must not fall back to them:
			// omitting ns/db on the credential sends {ac, key} with no ns/db keys at all,
			// which the server cannot resolve to a signin target. This matches the JS
			// SDK's AccessBearerAuth, which never falls back to session state.
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			surreal.query("DEFINE USER svcacct2 ON DATABASE PASSWORD 'pass' ROLES OWNER");
			surreal.query(
					"DEFINE ACCESS api_key2 ON DATABASE TYPE BEARER FOR USER DURATION FOR TOKEN 15m, FOR SESSION 12h");
			String key = grantKey(surreal.query("ACCESS api_key2 ON DATABASE GRANT FOR USER svcacct2").take(0));
			assertNotNull(key);

			SurrealException e = assertThrows(SurrealException.class,
					() -> surreal.signin(new AccessKeyCredential("api_key2", key)));
			assertNotNull(e.getMessage());
		}
	}

	@Test
	void signin_withAccessKey_invalidKey_throws() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			surreal.query("DEFINE USER svcacct3 ON DATABASE PASSWORD 'pass' ROLES OWNER");
			surreal.query(
					"DEFINE ACCESS api_key3 ON DATABASE TYPE BEARER FOR USER DURATION FOR TOKEN 15m, FOR SESSION 12h");

			SurrealException e = assertThrows(SurrealException.class,
					() -> surreal.signin(new AccessKeyCredential("test_ns", "test_db", "api_key3",
							"surreal-bearer-notarealid0-notarealkeyvalue00000000")));
			assertNotNull(e.getMessage());
		}
	}

	// -----------------------------------------------------------------
	// Gap 2: AccessCredential (signin via a named ACCESS method with
	// username/password, no arbitrary record variables)
	// -----------------------------------------------------------------

	@Test
	void signin_withAccessCredential_onSystemStyleAccess_succeeds() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			surreal.query("DEFINE TABLE svc SCHEMALESS");
			surreal.query("DEFINE ACCESS svc_access ON DATABASE TYPE RECORD "
					+ "SIGNIN ( SELECT * FROM svc WHERE name = $user AND crypto::argon2::compare(pass, $pass) ) "
					+ "DURATION FOR TOKEN 15m, FOR SESSION 12h");
			surreal.query("CREATE svc CONTENT { name: 'svcuser', pass: crypto::argon2::generate('secret') }");

			try (Surreal other = surreal.newSession()) {
				Token token = other
						.signin(new AccessCredential("svcuser", "secret", "svc_access", "test_ns", "test_db"));
				assertNotNull(token);
				assertNotNull(token.getAccess());
				Value access = other.query("RETURN $access").take(0);
				assertEquals("svc_access", access.getString());
			}
		}
	}

	@Test
	void signin_withAccessCredential_databaseWithoutNamespace_throws() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory");
			AccessCredential cred = new AccessCredential("u", "p", "some_access", null, "test_db");
			SurrealException e = assertThrows(SurrealException.class, () -> surreal.signin(cred));
			assertTrue(e.getMessage().contains("namespace"));
		}
	}

	@Test
	void signin_withAccessCredential_invalidCredentials_throws() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			surreal.query("DEFINE TABLE svc2 SCHEMALESS");
			surreal.query("DEFINE ACCESS svc_access2 ON DATABASE TYPE RECORD "
					+ "SIGNIN ( SELECT * FROM svc2 WHERE name = $user AND crypto::argon2::compare(pass, $pass) ) "
					+ "DURATION FOR TOKEN 15m, FOR SESSION 12h");
			surreal.query("CREATE svc2 CONTENT { name: 'realuser', pass: crypto::argon2::generate('secret') }");

			AccessCredential cred = new AccessCredential("realuser", "wrong_password", "svc_access2", "test_ns",
					"test_db");
			SurrealException e = assertThrows(SurrealException.class, () -> surreal.signin(cred));
			assertNotNull(e.getMessage());
		}
	}

	// -----------------------------------------------------------------
	// Gap 3: authenticate(Token) - plain re-authenticate vs. refresh rotation
	// -----------------------------------------------------------------

	@Test
	void authenticate_withRefreshToken_rotatesBothTokens() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			surreal.query("DEFINE TABLE user SCHEMALESS");
			surreal.query("DEFINE ACCESS user_refresh ON DATABASE TYPE RECORD "
					+ "SIGNUP ( CREATE user SET email = $email, pass = crypto::argon2::generate($pass) ) "
					+ "SIGNIN ( SELECT * FROM user WHERE email = $email AND crypto::argon2::compare(pass, $pass) ) "
					+ "WITH REFRESH " + "DURATION FOR TOKEN 15m, FOR SESSION 12h");

			Token signup = surreal
					.signup(new RecordCredential("user_refresh", params("email", "refresh@example.com", "pass", "p")));
			assertNotNull(signup.getAccess());
			assertNotNull(signup.getRefresh());

			// authenticate(Token) with a non-null refresh performs a real server-side
			// refresh: both tokens are validated and rotated, proving this is not an echo.
			Token rotated = surreal.authenticate(signup);
			assertNotNull(rotated.getAccess());
			assertNotNull(rotated.getRefresh());
			assertNotEquals(signup.getAccess(), rotated.getAccess());
			assertNotEquals(signup.getRefresh(), rotated.getRefresh());
		}
	}

	@Test
	void authenticate_withNullRefresh_returnsEchoWithoutRotation() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			surreal.query("DEFINE TABLE user_plain SCHEMALESS");
			surreal.query("DEFINE ACCESS user_plain_access ON DATABASE TYPE RECORD "
					+ "SIGNUP ( CREATE user_plain SET email = $email, pass = crypto::argon2::generate($pass) ) "
					+ "SIGNIN ( SELECT * FROM user_plain WHERE email = $email AND crypto::argon2::compare(pass, $pass) ) "
					+ "DURATION FOR TOKEN 15m, FOR SESSION 12h");

			Token signup = surreal.signup(
					new RecordCredential("user_plain_access", params("email", "plain@example.com", "pass", "p")));
			assertNotNull(signup.getAccess());
			assertNull(signup.getRefresh());

			// No refresh present: authenticate(Token) takes the plain path (same as
			// authenticate(String)) and echoes the access token back, with a null refresh.
			Token echoed = surreal.authenticate(new Token(signup.getAccess(), null));
			assertEquals(signup.getAccess(), echoed.getAccess());
			assertNull(echoed.getRefresh());
		}
	}

	@Test
	void authenticate_withEmptyRefresh_throwsClearExceptionRatherThanCrashing() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			// A non-null but empty refresh routes to the refresh path (since it is
			// non-null), where the native layer treats "no refresh content" as "missing
			// refresh token" - this must surface as a SurrealException, not crash.
			SurrealException e = assertThrows(SurrealException.class,
					() -> surreal.authenticate(new Token("some_access_value", "")));
			assertNotNull(e.getMessage());
		}
	}

}
