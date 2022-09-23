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
            SurrealDriver driver = new DefaultSurrealDriver("172.18.0.2", 8000, 5);
            driver.signIn("admin", "incorrect-password");
        });
    }

    @Test
    public void testNoDatabaseSelected() {
        assertThrows(SurrealNoDatabaseSelectedException.class, () -> {
            SurrealDriver driver = new DefaultSurrealDriver("172.18.0.2", 8000, 5);
            driver.signIn("root", "root");
            driver.select("person", Person.class);
        });
    }

}
