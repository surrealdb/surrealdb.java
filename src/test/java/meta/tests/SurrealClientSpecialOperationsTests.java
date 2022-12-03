package meta.tests;

import com.surrealdb.auth.SurrealRootCredentials;
import com.surrealdb.client.SurrealClient;
import com.surrealdb.client.SurrealClientSettings;
import com.surrealdb.client.SurrealTable;
import com.surrealdb.exception.SurrealAuthenticationException;
import com.surrealdb.exception.SurrealNoDatabaseSelectedException;
import lombok.extern.slf4j.Slf4j;
import meta.model.Person;
import meta.utils.TestUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Khalid Alharisi
 */
@Slf4j
public abstract class SurrealClientSpecialOperationsTests {

    private SurrealClient client;

    protected abstract @NotNull SurrealClient createClient(@NotNull SurrealClientSettings settings);

    @BeforeEach
    public void setup() {
        client = createClient(TestUtils.getClientSettings());

        log.info("Finished setup");
    }

    @AfterEach
    public void teardown() {
        log.info("Starting cleanup");

        client.cleanup();
    }

    @Test
    void ping_whenCalled_doesNotThrowException() {
        assertDoesNotThrow(() -> client.ping());
    }

    @Test
    @Disabled("Disabled until Surreal supports the version command")
    void getDatabaseVersion_whenCalled_returnsAValidSurrealVersion() {
        // Surreal uses the format '{}-{}' when responding to the 'version' RPC.
        assertTrue(client.databaseVersion().matches(".*-.*"));
    }

    @Test
    void signIn_whenCalledWithValidCredentials_doesNotThrowException() {
        assertDoesNotThrow(() -> client.signIn(TestUtils.getAuthCredentials()));
    }

    @Test
    void signIn_whenCalledWithInvalidCredentials_throwsException() {
        assertThrows(SurrealAuthenticationException.class, () -> {
            SurrealRootCredentials credentials = SurrealRootCredentials.from("invalid_username", "invalid_password");
            client.signIn(credentials);
        });
    }

    @Test
    void signOut_whenCalled_signsOut() {
        client.setNamespaceAndDatabase(TestUtils.getNamespace(), TestUtils.getDatabase());
        client.signOut();

        SurrealTable<Object> table = SurrealTable.of("generic_table", Object.class);
        Object record = new Object();

        assertThrows(SurrealAuthenticationException.class, () -> client.createRecord(table, record));
    }

    @Test
    void setNamespaceAndDatabase_whenProvidedWithValidValues_setsTheNameSpaceAndDatabase() {
        assertDoesNotThrow(() -> client.setNamespaceAndDatabase(TestUtils.getNamespace(), TestUtils.getDatabase()));
    }

    @Test
    void createRecord_whenCalledBeforeSettingNamespaceAndDatabase_throwsException() {
        client.signIn(TestUtils.getAuthCredentials());

        SurrealTable<Object> table = SurrealTable.of("generic_table", Object.class);
        Object record = new Object();

        assertThrows(SurrealNoDatabaseSelectedException.class, () -> client.createRecord(table, record));
    }
}
