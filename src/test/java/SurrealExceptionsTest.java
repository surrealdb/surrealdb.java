import com.surrealdb.java.connection.SurrealConnection;
import com.surrealdb.java.connection.SurrealWebSocketConnection;
import com.surrealdb.java.driver.DefaultSurrealDriver;
import com.surrealdb.java.driver.SurrealDriver;
import com.surrealdb.java.connection.exception.SurrealAuthenticationException;
import com.surrealdb.java.connection.exception.SurrealNoDatabaseSelectedException;
import lombok.extern.slf4j.Slf4j;
import model.Person;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
public class SurrealExceptionsTest {

    @Test
    public void testBadCredentials() {
        assertThrows(SurrealAuthenticationException.class, () -> {
            SurrealConnection connection = new SurrealWebSocketConnection("172.18.0.2", 8000);
            connection.connect(5);

            SurrealDriver driver = new DefaultSurrealDriver(connection);
            driver.signIn("admin", "incorrect-password");
        });
    }

    @Test
    public void testNoDatabaseSelected() {
        assertThrows(SurrealNoDatabaseSelectedException.class, () -> {
            SurrealConnection connection = new SurrealWebSocketConnection("172.18.0.2", 8000);
            connection.connect(5);

            SurrealDriver driver = new DefaultSurrealDriver(connection);
            driver.signIn("root", "root");
            driver.select("person", Person.class);
        });
    }

}
