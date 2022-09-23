import com.surrealdb.java.connection.SurrealConnection;
import com.surrealdb.java.connection.SurrealWebSocketConnection;
import com.surrealdb.java.connection.exception.SurrealAuthenticationException;
import com.surrealdb.java.connection.exception.SurrealNoDatabaseSelectedException;
import com.surrealdb.java.driver.DefaultSurrealDriver;
import com.surrealdb.java.driver.SurrealDriver;
import model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class SurrealSpecialOperationsTest {

    private SurrealDriver driver;

    @BeforeEach
    public void setup(){
        SurrealConnection connection = new SurrealWebSocketConnection(TestUtils.getHost(), TestUtils.getPort());
        connection.connect(5);
        driver = new DefaultSurrealDriver(connection);
    }

    @Test
    public void testSignIn() {
        driver.signIn("root", "root");
    }

    @Test
    public void testBadCredentials() {
        assertThrows(SurrealAuthenticationException.class, () -> {
            driver.signIn("admin", "incorrect-password");
        });
    }

    @Test
    public void testUse() {
        driver.use("test", "test");
    }

    @Test
    public void testNoDatabaseSelected() {
        assertThrows(SurrealNoDatabaseSelectedException.class, () -> {
            driver.signIn("root", "root");
            driver.select("person", Person.class);
        });
    }

    @Test
    public void testLet() {
        driver.let("someKey", "someValue");
    }

}
