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

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Khalid Alharisi
 */
public class SurrealDriverSpecialOperationsTest {

	private SyncSurrealDriver driver;

	@BeforeEach
	public void setup() {
		SurrealConnection connection = new SurrealWebSocketConnection(TestUtils.getHost(), TestUtils.getPort(), false);
		connection.connect(5);
		driver = new SyncSurrealDriver(connection);
	}

	@Test
	public void testSignIn() {
		signIn(driver);
	}

	@Test
	public void testWrongLoginType() {
		switch (TestUtils.getAuthenticationType()) {
			case ROOT -> {
				assertThrows(SurrealAuthenticationException.class, () -> driver.signIn(TestUtils.getUsername(), TestUtils.getPassword(), "ns"));
				assertThrows(SurrealAuthenticationException.class, () -> driver.signIn(TestUtils.getUsername(), TestUtils.getPassword(), "ns", "db"));
			}
			case NAMESPACE -> {
				assertThrows(SurrealAuthenticationException.class, () -> driver.signIn(TestUtils.getUsername(), TestUtils.getPassword()));
				assertThrows(SurrealAuthenticationException.class, () -> driver.signIn(TestUtils.getUsername(), TestUtils.getPassword(), "ns", "db"));
			}
			case DATABASE -> {
				assertThrows(SurrealAuthenticationException.class, () -> driver.signIn(TestUtils.getUsername(), TestUtils.getPassword()));
				assertThrows(SurrealAuthenticationException.class, () -> driver.signIn(TestUtils.getUsername(), TestUtils.getPassword(), "ns"));
			}
		}
	}

	@Test
	public void testBadCredentials() {
		assertThrows(SurrealAuthenticationException.class, () -> driver.signIn("admin", "incorrect-password"));
	}

	@Test
	public void testUse() {
		driver.use(TestUtils.getNamespace(), TestUtils.getDatabase());
	}

	@Test
	public void testNoDatabaseSelected() {
		// When authentication on the database, the database is automatically selected
		if (TestUtils.getAuthenticationType() == TestUtils.AuthenticationType.DATABASE) return;

		assertThrows(SurrealNoDatabaseSelectedException.class, () -> {
			signIn(driver);
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
		signIn(driver);
		driver.use(TestUtils.getNamespace(), TestUtils.getDatabase());
		driver.info();
	}

	private void signIn(SyncSurrealDriver driver) {
		switch (TestUtils.getAuthenticationType()) {
			case ROOT -> driver.signIn(TestUtils.getUsername(), TestUtils.getPassword());
			case NAMESPACE -> driver.signIn(TestUtils.getUsername(), TestUtils.getPassword(), TestUtils.getNamespace());
			case DATABASE ->
				driver.signIn(TestUtils.getUsername(), TestUtils.getPassword(), TestUtils.getNamespace(), TestUtils.getDatabase());
		}
	}

}
