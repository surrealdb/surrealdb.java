package com.surrealdb.driver;

import com.surrealdb.BaseIntegrationTest;
import com.surrealdb.TestUtils;
import com.surrealdb.connection.SurrealWebSocketConnection;
import com.surrealdb.connection.exception.SurrealAuthenticationException;
import com.surrealdb.connection.exception.SurrealNoDatabaseSelectedException;
import com.surrealdb.driver.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

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
        this.driver = new SyncSurrealDriver(connection);
    }

    @Test
    public void testSignIn() {
        this.driver.signIn(TestUtils.getUsername(), TestUtils.getPassword());
    }

    @Test
    public void testSignUp() {
        // Configure db to allow signup
        this.driver.signIn("root", "root"); // This needs to be configured from @BaseIntegrationTest
        // Set namespace and database to something random so it doesnt conflict with other tests
        // also - use driver settings instead of query
        this.driver.query(
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
                this.driver.signUp("testns", "testdb", "allusers", "test@testerino.surr", "lol123");
        // Validate that the signup worked through authentication with the received token.
        this.driver.authenticate(token);
    }

    @Test
    public void testAuthenticate() {
        if (TestUtils.getToken().equals("")) {
            return;
        }
        this.driver.authenticate(TestUtils.getToken());
    }

    @Test
    public void testBadCredentials() {
        assertThrows(
                SurrealAuthenticationException.class,
                () -> this.driver.signIn("admin", "incorrect-password"));
    }

    @Test
    public void testUse() {
        this.driver.use(TestUtils.getNamespace(), TestUtils.getDatabase());
    }

    @Test
    public void testNoDatabaseSelected() {
        assertThrows(
                SurrealNoDatabaseSelectedException.class,
                () -> {
                    this.driver.signIn(TestUtils.getUsername(), TestUtils.getPassword());
                    this.driver.select("person", Person.class);
                });
    }

    @Test
    public void testLet() {
        this.driver.let("someKey", "someValue");
    }

    @Test
    public void testPing() {
        this.driver.ping();
    }

    @Test
    public void testInfo() {
        this.driver.signIn(TestUtils.getUsername(), TestUtils.getPassword());
        this.driver.use(TestUtils.getNamespace(), TestUtils.getDatabase());
        this.driver.info();
    }

    @Test
    public void testInvalidate() {
        // Execute the test at the end, or re-connect after it (the method invalidates the current
        // session auth.)!
        this.driver.invalidate();
    }
}
