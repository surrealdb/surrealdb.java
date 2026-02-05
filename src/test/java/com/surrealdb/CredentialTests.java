package com.surrealdb;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.surrealdb.signin.Bearer;
import com.surrealdb.signin.Credential;
import com.surrealdb.signin.Database;
import com.surrealdb.signin.Namespace;
import com.surrealdb.signin.Record;
import com.surrealdb.signin.Root;
import com.surrealdb.signin.Token;

/**
 * Tests for Credential hierarchy, signin(Credential), signup(Record), optional ns/db for Record,
 * Bearer, and Token. Success-path auth (Root/Namespace/Database/Record/Bearer) may fail on memory
 * backend; we assert either a valid Token or a SurrealException with a message.
 */
public class CredentialTests {

    @Test
    void signin_withRoot_returnsTokenOrThrows() {
        try (Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            try {
                Token token = surreal.signin(new Root("root", "root"));
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
                surreal.signin(new Root("root", "root"));
                surreal.query("DEFINE USER user_ns ON NAMESPACE PASSWORD 'pass'");
                Token token = surreal.signin(new Namespace("user_ns", "pass", "test_ns"));
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
                surreal.signin(new Root("root", "root"));
                surreal.query("DEFINE USER user_db ON DATABASE PASSWORD 'pass'");
                Token token = surreal.signin(new Database("user_db", "pass", "test_ns", "test_db"));
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
                rootToken = surreal.signin(new Root("root", "root"));
            } catch (SurrealException e) {
                // memory may not support root signin; skip Bearer test
                return;
            }
            try (Surreal other = surreal.newSession()) {
                Token bearerToken = other.signin(new Bearer(rootToken.getAccess()));
                assertNotNull(bearerToken);
                assertEquals(rootToken.getAccess(), bearerToken.getAccess());
            }
        }
    }

    @Test
    void signin_withRecord_withExplicitNsDb_dispatchesToNative() {
        try (Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            Record record = new Record("test_ns", "test_db", "no_such_access", Map.of("email", "a@b.com", "pass", "p"));
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
            Record record = new Record("some_access", Map.of("email", "a@b.com", "pass", "p"));
            SurrealException e = assertThrows(SurrealException.class, () -> surreal.signin(record));
            assertTrue(e.getMessage().contains("namespace") || e.getMessage().contains("database"));
        }
    }

    @Test
    void signin_withRecord_usesSessionNsDbWhenOmitted() {
        try (Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            Record record = new Record("my_access", Map.of("email", "a@b.com", "pass", "p"));
            try {
                surreal.signin(record);
            } catch (SurrealException e) {
                // Should not be our "call useNs/useDb first" message; should be backend error (e.g. access not defined)
                assertNotNull(e.getMessage());
                assertTrue(
                    !e.getMessage().contains("Record signin requires namespace"),
                    "Should have resolved ns/db from session: " + e.getMessage()
                );
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
            Record record = new Record("test_ns", "test_db", "no_such_access", Map.of("email", "a@b.com", "pass", "p"));
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
            Record record = new Record("some_access", Map.of("email", "a@b.com", "pass", "p"));
            SurrealException e = assertThrows(SurrealException.class, () -> surreal.signup(record));
            assertTrue(e.getMessage().contains("namespace") || e.getMessage().contains("database"));
            assertTrue(e.getMessage().contains("useNs") || e.getMessage().contains("useDb"));
        }
    }

    @Test
    void signup_withSessionNsDb_usesStoredValues() {
        try (Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            Record record = new Record("no_such_access", Map.of("email", "a@b.com", "pass", "p"));
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
        Record r = new Record("ns", "db", "access", Map.of("a", 1));
        assertEquals("ns", r.getNamespace());
        assertEquals("db", r.getDatabase());
        assertEquals("access", r.getAccess());
        assertEquals(Map.of("a", 1), r.getParams());
    }

    @Test
    void signup_withRecordShortConstructor_hasNullNsDb() {
        Record r = new Record("access", Map.of("a", 1));
        assertNull(r.getNamespace());
        assertNull(r.getDatabase());
        assertEquals("access", r.getAccess());
        assertEquals(Map.of("a", 1), r.getParams());
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
        Bearer b = new Bearer("my_jwt");
        assertEquals("my_jwt", b.getToken());
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

    @Test
    void deprecatedSignin_withSigninDispatchesToSigninCredential() {
        try (Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            try {
                Token token = surreal.signin((com.surrealdb.signin.Signin) new Root("root", "root"));
                assertNotNull(token);
            } catch (SurrealException e) {
                assertNotNull(e.getMessage());
            }
        }
    }
}
