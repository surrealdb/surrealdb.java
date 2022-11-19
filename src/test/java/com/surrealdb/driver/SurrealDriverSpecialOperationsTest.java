package com.surrealdb.driver;

import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.connection.exception.SurrealAuthenticationException;
import com.surrealdb.connection.exception.SurrealNoDatabaseSelectedException;
import com.surrealdb.driver.auth.SurrealRootCredentials;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import com.surrealdb.meta.utils.TestUtils;
import com.surrealdb.meta.model.Person;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Khalid Alharisi
 */
public class SurrealDriverSpecialOperationsTest {

    private SurrealDriver driver;

    @BeforeEach
    public void setup() {
        SurrealConnection connection = SurrealConnection.create(TestUtils.getConnectionSettings());
        connection.connect(5);
        driver = SurrealDriver.create(connection);
    }

    @AfterEach
    public void teardown() {
        SurrealConnection connection = driver.getSurrealConnection();
        connection.disconnect();
    }

    @Test
    void testPing() {
        assertDoesNotThrow(() -> driver.ping());
    }

    @Test
    @Disabled("Disabled until Surreal supports the version command")
    void testGetDatabaseVersion() {
        // Surreal uses the format '{}-{}' when responding to the 'version' RPC.
        assertTrue(driver.databaseVersion().matches(".*-.*"));
    }

    @Test
    void testSignIn() {
        assertDoesNotThrow(() -> driver.signIn(TestUtils.getAuthCredentials()));
    }

    @Test
    void testBadCredentials() {
        assertThrows(SurrealAuthenticationException.class, () -> {
            driver.signIn(SurrealRootCredentials.from("invalid_username", "invalid_password"));
        });
    }

    @Test
    void testUse() {
        assertDoesNotThrow(() -> driver.use(TestUtils.getNamespace(), TestUtils.getDatabase()));
    }

    @Test
    void testNoDatabaseSelected() {
        driver.signIn(TestUtils.getAuthCredentials());

        assertThrows(SurrealNoDatabaseSelectedException.class, () -> {
            driver.retrieveAllRecordsFromTable(SurrealTable.of("person", Person.class));
        });
    }
}
