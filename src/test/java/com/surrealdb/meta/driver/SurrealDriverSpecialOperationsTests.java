package com.surrealdb.meta.driver;

import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.connection.exception.SurrealAuthenticationException;
import com.surrealdb.connection.exception.SurrealNoDatabaseSelectedException;
import com.surrealdb.driver.SurrealDriver;
import com.surrealdb.driver.SurrealDriverSettings;
import com.surrealdb.driver.SurrealTable;
import com.surrealdb.driver.auth.SurrealRootCredentials;
import com.surrealdb.meta.model.Person;
import com.surrealdb.meta.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

/**
 * @author Khalid Alharisi
 */
public abstract class SurrealDriverSpecialOperationsTests {

    private SurrealDriver driver;

    protected abstract SurrealDriver createDriver(SurrealConnection connection, SurrealDriverSettings settings);

    @BeforeEach
    public void setup() {
        SurrealConnection connection = SurrealConnection.create(TestUtils.getConnectionSettings());
        connection.connect(5);
        driver = createDriver(connection, SurrealDriverSettings.DEFAULT);
    }

    @AfterEach
    public void teardown() {
        SurrealConnection connection = driver.getSurrealConnection();
        connection.disconnect();
    }

    @Test
    void ping_whenCalled_doesNotThrowException() {
        assertDoesNotThrow(() -> driver.ping());
    }

    @Test
    @Disabled("Disabled until Surreal supports the version command")
    void getDatabaseVersion_whenCalled_returnsAValidSurrealVersion() {
        // Surreal uses the format '{}-{}' when responding to the 'version' RPC.
        assertTrue(driver.databaseVersion().matches(".*-.*"));
    }

    @Test
    void signIn_whenCalledWithValidCredentials_doesNotThrowException() {
        assertDoesNotThrow(() -> driver.signIn(TestUtils.getAuthCredentials()));
    }

    @Test
    void signIn_whenCalledWithInvalidCredentials_throwsException() {
        assertThrows(SurrealAuthenticationException.class, () -> {
            SurrealRootCredentials credentials = SurrealRootCredentials.from("invalid_username", "invalid_password");
            driver.signIn(credentials);
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
