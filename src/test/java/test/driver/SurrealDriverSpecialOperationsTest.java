package test.driver;

import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.connection.exception.SurrealAuthenticationException;
import com.surrealdb.connection.exception.SurrealNoDatabaseSelectedException;
import com.surrealdb.driver.auth.SurrealAuthCredentials;
import com.surrealdb.driver.SyncSurrealDriver;
import com.surrealdb.driver.auth.SurrealRootCredentials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import test.TestUtils;
import test.driver.model.Person;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Khalid Alharisi
 */
public class SurrealDriverSpecialOperationsTest {

    private SyncSurrealDriver driver;

    @BeforeEach
    public void setup() {
        SurrealConnection connection = SurrealConnection.create(TestUtils.getConnectionSettings());
        connection.connect(5);
        driver = new SyncSurrealDriver(connection);
    }

    @Test
    public void testSignIn() {
        driver.signIn(TestUtils.getRootCredentials());
    }

    @Test
    public void testBadCredentials() {
        assertThrows(SurrealAuthenticationException.class, () -> {
            driver.signIn(SurrealRootCredentials.from("invalid_username", "invalid_password"));
        });
    }

    @Test
    public void testUse() {
        driver.use(TestUtils.getNamespace(), TestUtils.getDatabase());
    }

    @Test
    public void testNoDatabaseSelected() {
        assertThrows(SurrealNoDatabaseSelectedException.class, () -> {
            driver.signIn(TestUtils.getRootCredentials());
            driver.select("person", Person.class);
        });
    }

    @Test
    public void testLet() {
        driver.setConnectionWideParameter("someKey", "someValue");
    }

    @Test
    public void testPing() {
        driver.ping();
    }

    @Test
    @Disabled("Disabled until Surreal supports the version command")
    void testGetDatabaseVersion() {
        // Surreal uses the format '{}-{}' when responding to the 'version' RPC.
        assertTrue(driver.getDatabaseVersion().matches(".*-.*"));
    }

    @Test
    public void testInfo() {
        driver.signIn(TestUtils.getRootCredentials());
        driver.use(TestUtils.getNamespace(), TestUtils.getDatabase());
        driver.info();
    }

}
