import com.surrealdb.java.connection.SurrealConnection;
import com.surrealdb.java.connection.SurrealWebSocketConnection;
import com.surrealdb.java.connection.exception.SurrealAuthenticationException;
import com.surrealdb.java.connection.exception.SurrealNoDatabaseSelectedException;
import com.surrealdb.java.driver.SyncSurrealDriver;
import model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class SurrealSpecialOperationsTest {

    private SyncSurrealDriver driver;

    @BeforeEach
    public void setup(){
        SurrealConnection connection = new SurrealWebSocketConnection(TestUtils.getHost(), TestUtils.getPort());
        connection.connect(5);
        driver = new SyncSurrealDriver(connection);
    }

    @Test
    public void testSignIn() {
        driver.signIn(TestUtils.getUsername(), TestUtils.getPassword());
    }

    @Test
    public void testBadCredentials() {
        assertThrows(SurrealAuthenticationException.class, () -> {
            driver.signIn("admin", "incorrect-password");
        });
    }

    @Test
    public void testUse() {
        driver.use(TestUtils.getNamespace(), TestUtils.getDatabase());
    }

    @Test
    public void testNoDatabaseSelected() {
        assertThrows(SurrealNoDatabaseSelectedException.class, () -> {
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

}
