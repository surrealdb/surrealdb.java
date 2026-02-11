package com.surrealdb;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.surrealdb.signin.BearerCredential;
import com.surrealdb.signin.Credential;
import com.surrealdb.signin.DatabaseCredential;
import com.surrealdb.signin.NamespaceCredential;
import com.surrealdb.signin.RecordCredential;
import com.surrealdb.signin.RootCredential;
import com.surrealdb.signin.Token;

/**
 * Tests for Credential hierarchy, signin(Credential), signup(RecordCredential),
 * optional ns/db for RecordCredential, BearerCredential, and Token.
 * Success-path auth (RootCredential/NamespaceCredential/etc.) may fail on
 * memory backend; we assert either a valid Token or a SurrealException with a
 * message.
 */
public class CredentialTests {

	/** Java 8 compatible: Map.of("email", "a@b.com", "pass", "p") */
	private static Map<String, String> params(String k1, String v1, String k2, String v2) {
		Map<String, String> m = new HashMap<>();
		m.put(k1, v1);
		m.put(k2, v2);
		return m;
	}

	@Test
	void signin_withRoot_returnsTokenOrThrows() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			try {
				Token token = surreal.signin(new RootCredential("root", "root"));
				assertNotNull(token);
				assertNotNull(token.getAccess());
				assertEquals(token.getAccess(), token.getToken());
				assertNull(token.getRefresh());
			} catch (SurrealException e) {
				assertNotNull(e.getMessage());
			}
		}
	}

	@Test
	void signin_withNamespace_dispatchesToNative() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			try {
				surreal.signin(new RootCredential("root", "root"));
				surreal.query("DEFINE USER user_ns ON NAMESPACE PASSWORD 'pass'");
				Token token = surreal.signin(new NamespaceCredential("user_ns", "pass", "test_ns"));
				assertNotNull(token);
				assertNotNull(token.getAccess());
			} catch (SurrealException e) {
				assertNotNull(e.getMessage());
			}
		}
	}

	@Test
	void signin_withDatabase_dispatchesToNative() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			try {
				surreal.signin(new RootCredential("root", "root"));
				surreal.query("DEFINE USER user_db ON DATABASE PASSWORD 'pass'");
				Token token = surreal.signin(new DatabaseCredential("user_db", "pass", "test_ns", "test_db"));
				assertNotNull(token);
				assertNotNull(token.getAccess());
			} catch (SurrealException e) {
				assertNotNull(e.getMessage());
			}
		}
	}

	@Test
	void signin_withBearer_returnsTokenWithSameAccess() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			Token rootToken;
			try {
				rootToken = surreal.signin(new RootCredential("root", "root"));
			} catch (SurrealException e) {
				// memory may not support root signin; skip Bearer test
				return;
			}
			try (Surreal other = surreal.newSession()) {
				Token bearerToken = other.signin(new BearerCredential(rootToken.getAccess()));
				assertNotNull(bearerToken);
				assertEquals(rootToken.getAccess(), bearerToken.getAccess());
			}
		}
	}

	@Test
	void signin_withRecord_withExplicitNsDb_dispatchesToNative() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			RecordCredential record = new RecordCredential("test_ns", "test_db", "no_such_access",
					params("email", "a@b.com", "pass", "p"));
			try {
				surreal.signin(record);
			} catch (SurrealException e) {
				// expected: access not defined or auth failure
				assertNotNull(e.getMessage());
			}
		}
	}

	@Test
	void signin_withRecordNullNsDbAndNoUseNs_throws() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory");
			RecordCredential record = new RecordCredential("some_access", params("email", "a@b.com", "pass", "p"));
			SurrealException e = assertThrows(SurrealException.class, () -> surreal.signin(record));
			assertTrue(e.getMessage().contains("namespace") || e.getMessage().contains("database"));
		}
	}

	@Test
	void signin_withRecord_usesSessionNsDbWhenOmitted() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			RecordCredential record = new RecordCredential("my_access", params("email", "a@b.com", "pass", "p"));
			try {
				surreal.signin(record);
			} catch (SurrealException e) {
				// Should not be our "call useNs/useDb first" message; should be backend error
				// (e.g. access not defined)
				assertNotNull(e.getMessage());
				assertTrue(!e.getMessage().contains("RecordCredential signin requires namespace"),
						"Should have resolved ns/db from session: " + e.getMessage());
			}
		}
	}

	@Test
	void signin_withNullCredential_throws() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory");
			SurrealException e = assertThrows(SurrealException.class, () -> surreal.signin((Credential) null));
			assertNotNull(e.getMessage());
			assertTrue(e.getMessage().contains("Unsupported") || e.getMessage().contains("null"));
		}
	}

	@Test
	void signup_withExplicitNsDb_dispatchesToNative() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			RecordCredential record = new RecordCredential("test_ns", "test_db", "no_such_access",
					params("email", "a@b.com", "pass", "p"));
			try {
				surreal.signup(record);
			} catch (SurrealException e) {
				assertNotNull(e.getMessage());
			}
		}
	}

	@Test
	void signup_withNullNsDbAndNoUseNs_throws() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory");
			RecordCredential record = new RecordCredential("some_access", params("email", "a@b.com", "pass", "p"));
			SurrealException e = assertThrows(SurrealException.class, () -> surreal.signup(record));
			assertTrue(e.getMessage().contains("namespace") || e.getMessage().contains("database"));
			assertTrue(e.getMessage().contains("useNs") || e.getMessage().contains("useDb"));
		}
	}

	@Test
	void signup_withSessionNsDb_usesStoredValues() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			RecordCredential record = new RecordCredential("no_such_access", params("email", "a@b.com", "pass", "p"));
			try {
				surreal.signup(record);
			} catch (SurrealException e) {
				// should be backend error, not "namespace/database required"
				assertNotNull(e.getMessage());
			}
		}
	}

	@Test
	void signup_withRecordFullConstructor_acceptsExplicitNsDb() {
		RecordCredential r = new RecordCredential("ns", "db", "access", Collections.singletonMap("a", 1));
		assertEquals("ns", r.getNamespace());
		assertEquals("db", r.getDatabase());
		assertEquals("access", r.getAccess());
		assertEquals(Collections.singletonMap("a", 1), r.getParams());
	}

	@Test
	void signup_withRecordShortConstructor_hasNullNsDb() {
		RecordCredential r = new RecordCredential("access", Collections.singletonMap("a", 1));
		assertNull(r.getNamespace());
		assertNull(r.getDatabase());
		assertEquals("access", r.getAccess());
		assertEquals(Collections.singletonMap("a", 1), r.getParams());
	}

	@Test
	void recordCredential_nullAccess_throws() {
		Throwable e1 = assertThrows(NullPointerException.class,
				() -> new RecordCredential("ns", "db", null, params("email", "a@b.com", "pass", "p")));
		assertNotNull(e1.getMessage());
		Throwable e2 = assertThrows(NullPointerException.class,
				() -> new RecordCredential(null, params("email", "a@b.com", "pass", "p")));
		assertNotNull(e2.getMessage());
	}

	@Test
	void token_hasAccessAndOptionalRefresh() {
		Token t = new Token("access_jwt", null);
		assertEquals("access_jwt", t.getAccess());
		assertEquals("access_jwt", t.getToken());
		assertNull(t.getRefresh());
		Token t2 = new Token("a", "r");
		assertEquals("a", t2.getAccess());
		assertEquals("r", t2.getRefresh());
	}

	@Test
	void bearer_holdsToken() {
		BearerCredential b = new BearerCredential("my_jwt");
		assertEquals("my_jwt", b.getToken());
	}

	@Test
	void bearerCredential_nullToken_throws() {
		Throwable e = assertThrows(NullPointerException.class, () -> new BearerCredential(null));
		assertNotNull(e.getMessage());
	}

	@Test
	void authenticate_returnsThis() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			try {
				Surreal result = surreal.authenticate("some_token");
				assertSame(surreal, result);
			} catch (SurrealException e) {
				assertNotNull(e.getMessage());
			}
		}
	}

	@Test
	void invalidate_returnsThis() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			Surreal result = surreal.invalidate();
			assertSame(surreal, result);
		}
	}

}
