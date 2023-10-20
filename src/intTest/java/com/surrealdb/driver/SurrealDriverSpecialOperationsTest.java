package com.surrealdb.driver;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.surrealdb.BaseIntegrationTest;
import com.surrealdb.TestUtils;
import com.surrealdb.connection.SurrealWebSocketConnection;
import com.surrealdb.connection.exception.SurrealAuthenticationException;
import com.surrealdb.connection.exception.SurrealNoDatabaseSelectedException;
import com.surrealdb.driver.model.Person;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * @author Khalid Alharisi
 */
@Testcontainers
public class SurrealDriverSpecialOperationsTest extends BaseIntegrationTest {

    private SyncSurrealDriver driver;

    @BeforeEach
    public void setup() {
        final SurrealWebSocketConnection connection =
                new SurrealWebSocketConnection(testHost, testPort, false);
        connection.connect(5);
        driver = new SyncSurrealDriver(connection);
    }

    @Test
    public void testSignIn() {
        driver.signIn(TestUtils.getUsername(), TestUtils.getPassword());
    }

    @Test
    public void testSignUp() {
        // Configure db to allow signup
        driver.signIn("root", "root"); // This needs to be configured from @BaseIntegrationTest
        // Set namespace and database to something random, so it doesn't conflict with other tests
        // also - use driver settings instead of query
        driver.query(
                """
        USE NAMESPACE testns;
        USE DATABASE testdb;
        DEFINE SCOPE allusers SESSION 24h
            SIGNUP ( CREATE user SET user = $user, pass = crypto::argon2::generate($pass))
            SIGNIN ( SELECT * FROM user where email = $user AND crypto::argon2::compare(pass, $pass));
        """,
                Map.of(),
                Object.class);

        // Plain
        final String token =
                driver.signUp("testns", "testdb", "allusers", "test@testerino.surr", "lol123");
        // Validate that the signup worked through authentication with the received token.
        driver.authenticate(token);
    }

    @Test
    public void testAuthenticate() {
        if (TestUtils.getToken().isEmpty()) {
            return;
        }
        driver.authenticate(TestUtils.getToken());
    }

    @Test
    public void testBadCredentials() {
        assertThrows(
                SurrealAuthenticationException.class,
                () -> driver.signIn("admin", "incorrect-password"));
    }

    @Test
    public void testUse() {
        driver.use(TestUtils.getNamespace(), TestUtils.getDatabase());
    }

    @Test
    public void testNoDatabaseSelected() {
        assertThrows(
                SurrealNoDatabaseSelectedException.class,
                () -> {
                    driver.signIn(TestUtils.getUsername(), TestUtils.getPassword());
                    driver.select("person", Person.class);
                });
    }

    @Test
    public void testLet() {
        driver.let("someKey", "someValue");
    }

    @Test
    public void testPing() {
        driver.ping();
    }

    @Test
    public void testInfo() {
        driver.signIn(TestUtils.getUsername(), TestUtils.getPassword());
        driver.use(TestUtils.getNamespace(), TestUtils.getDatabase());
        driver.info();
    }

    @Test
    public void testInvalidate() {
        // Execute the test at the end, or re-connect after it (the method invalidates the current
        // session auth.)!
        driver.invalidate();
    }
}
