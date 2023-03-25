package com.surrealdb.connection;

import com.surrealdb.connection.exception.SurrealConnectionTimeoutException;
import com.surrealdb.connection.exception.SurrealNotConnectedException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Khalid Alharisi
 */
@Slf4j
@Testcontainers
public class SurrealConnectionTest {

    @Container
    private final GenericContainer surrealDb = new GenericContainer(DockerImageName.parse("surrealdb/surrealdb:latest"))
        .withExposedPorts(8000).withCommand("start --log trace --user root --pass root memory");
    private SurrealWebSocketConnection connection;

    @BeforeEach
    public void setUp() {
        connection = new SurrealWebSocketConnection(surrealDb.getHost(), surrealDb.getFirstMappedPort(), false);
    }

    @Test
    public void testConnectSuccessfully() {
        assertDoesNotThrow(() -> connection.connect(3));
    }

    @Test
    public void testHostNotReachable1() {
        assertThrows(SurrealConnectionTimeoutException.class, () -> {
            SurrealConnection connection = new SurrealWebSocketConnection("172.18.0.10", 8000, false);
            connection.connect(3);
        });
    }

    @Test
    public void testHostNotReachable2() {
        assertThrows(SurrealConnectionTimeoutException.class, () -> {
            SurrealConnection connection = new SurrealWebSocketConnection("localhost", 9999, false);
            connection.connect(3);
        });
    }

    @Test
    public void testInvalidHostname() {
        assertThrows(SurrealConnectionTimeoutException.class, () -> {
            SurrealConnection connection = new SurrealWebSocketConnection("some_hostname", 8000, false);
            connection.connect(3);
        });
    }

    @Test
    public void testUserForgotToConnect() {
        assertThrows(SurrealNotConnectedException.class, () -> connection.rpc(null, "let", "some_key", "some_val"));
    }

    @Test
    public void testUserConnectsThenDisconnects() {
        assertThrows(SurrealNotConnectedException.class, () -> {
            connection.connect(3);
            connection.disconnect();
            connection.rpc(null, "let", "some_key", "some_val");
        });
    }


}
