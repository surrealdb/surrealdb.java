package test.driver;

import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.connection.SurrealWebSocketConnection;
import com.surrealdb.connection.exception.SurrealAuthenticationException;
import com.surrealdb.connection.exception.SurrealNoDatabaseSelectedException;
import com.surrealdb.driver.SyncSurrealDriver;
import test.TestUtils;
import test.driver.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Khalid Alharisi
 */
public class SurrealDriverSpecialOperationsTest {

    private SyncSurrealDriver driver;

    @BeforeEach
    public void setup(){
        SurrealConnection connection = new SurrealWebSocketConnection(TestUtils.getHost(), TestUtils.getPort(), false);
        connection.connect(5);
        driver = new SyncSurrealDriver(connection);
    }

    @Test
    public void testSignIn() {
        driver.signIn(TestUtils.getUsername(), TestUtils.getPassword());
    }

	@Test
	public void testSignUp() {
		List<String> receivedJwt = new ArrayList<>();
		//Plain
		receivedJwt.add(driver.signUp(TestUtils.getNamespace(), TestUtils.getDatabase(), TestUtils.getScope(), "test@testerino.surr", "lol123"));
		//With marketing
		receivedJwt.add(driver.signUp(TestUtils.getNamespace(), TestUtils.getDatabase(), TestUtils.getScope(), "test1@testerino.surr", "lol123", true));
		//With tags
		receivedJwt.add(driver.signUp(TestUtils.getNamespace(), TestUtils.getDatabase(), TestUtils.getScope(), "test2@testerino.surr", "lol123", new String[] { "Java", "Rust" }));
		//With marketing and tags
		receivedJwt.add(driver.signUp(TestUtils.getNamespace(), TestUtils.getDatabase(), TestUtils.getScope(), "test3@testerino.surr", "lol123", true, new String[] { "Java" }));

		//Validate that the signup worked through authentication with the received token.
		receivedJwt.forEach(token -> driver.authenticate(token));
	}

	@Test
	public void testAuthenticate() {
		if (TestUtils.getToken().equals("")) {
			return;
		}
		driver.authenticate(TestUtils.getToken());
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
