package meta.tests;

import com.surrealdb.SurrealClient;
import com.surrealdb.SurrealClientSettings;
import com.surrealdb.SurrealTable;
import com.surrealdb.auth.SurrealRootCredentials;
import com.surrealdb.exception.SurrealAuthenticationException;
import com.surrealdb.exception.SurrealNoDatabaseSelectedException;
import meta.model.Person;
import meta.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Khalid Alharisi
 */
public abstract class SurrealClientSpecialOperationsTests {

    private SurrealClient client;

    protected abstract SurrealClient createClient(SurrealClientSettings settings);

    @BeforeEach
    public void setup() {
        client = createClient(TestUtils.getClientSettings());
        client.connect(3, TimeUnit.SECONDS);
    }

    @AfterEach
    public void teardown() {
        client.disconnect();
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
    void testUse() {
        assertDoesNotThrow(() -> client.use(TestUtils.getNamespace(), TestUtils.getDatabase()));
    }

    @Test
    void testNoDatabaseSelected() {
        client.signIn(TestUtils.getAuthCredentials());

        assertThrows(SurrealNoDatabaseSelectedException.class, () -> {
            client.retrieveAllRecordsFromTable(SurrealTable.of("person", Person.class));
        });
    }
}
