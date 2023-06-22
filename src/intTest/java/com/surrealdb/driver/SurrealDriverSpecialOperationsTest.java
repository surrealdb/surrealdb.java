package com.surrealdb.driver;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.surrealdb.BaseIntegrationTest;
import com.surrealdb.TestUtils;
import com.surrealdb.connection.SurrealWebSocketConnection;
import com.surrealdb.connection.exception.SurrealAuthenticationException;
import com.surrealdb.connection.exception.SurrealNoDatabaseSelectedException;
import com.surrealdb.driver.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * @author Khalid Alharisi
 */
@Testcontainers
public class SurrealDriverSpecialOperationsTest extends BaseIntegrationTest {

    private SyncSurrealDriver driver;

    @BeforeEach
    public void setup() {
        SurrealWebSocketConnection connection = new SurrealWebSocketConnection(testHost, testPort, false);
        connection.connect(5);
        driver = new SyncSurrealDriver(connection);
    }

    @Test
    public void testSignIn() {
        driver.signIn(TestUtils.getUsername(), TestUtils.getPassword());
    }

    @Test
    public void testSignUp() {
        // Plain
        String token =
                driver.signUp(
                        TestUtils.getNamespace(),
                        TestUtils.getDatabase(),
                        TestUtils.getScope(),
                        "test@testerino.surr",
                        "lol123");
        // Validate that the signup worked through authentication with the received token.
        driver.authenticate(token);
    }

    @Test
    public void testAuthenticate() {
        if (TestUtils.getToken().equals("")) {
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
