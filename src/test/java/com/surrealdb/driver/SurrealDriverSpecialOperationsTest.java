package com.surrealdb.driver;

import com.surrealdb.TestUtils;
import com.surrealdb.connection.SurrealWebSocketConnection;
import com.surrealdb.connection.exception.SurrealAuthenticationException;
import com.surrealdb.connection.exception.SurrealNoDatabaseSelectedException;
import com.surrealdb.driver.model.Person;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Khalid Alharisi
 */
@Testcontainers
public class SurrealDriverSpecialOperationsTest {
    @Container
    private static final GenericContainer surrealDb = new GenericContainer(DockerImageName.parse("surrealdb/surrealdb:latest"))
        .withExposedPorts(8000).withCommand("start --log trace --user root --pass root memory");
    private SurrealWebSocketConnection connection;
    private SyncSurrealDriver driver;

    @BeforeEach
    public void setup(){
        connection = new SurrealWebSocketConnection(surrealDb.getHost(), surrealDb.getFirstMappedPort(), false);
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
