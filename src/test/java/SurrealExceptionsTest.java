import com.surrealdb.java.DefaultSurreal;
import com.surrealdb.java.Surreal;
import com.surrealdb.java.connection.exception.SurrealAuthenticationException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
public class SurrealExceptionsTest {

    @Test
    public void testBadCredentials() {
        assertThrows(SurrealAuthenticationException.class, () -> {
            Surreal surreal = new DefaultSurreal("172.18.0.2", 8000, 5);
            surreal.signIn("admin", "incorrect-password");
        });
    }

}
