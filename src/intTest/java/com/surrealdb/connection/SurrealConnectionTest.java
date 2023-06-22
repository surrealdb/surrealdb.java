package com.surrealdb.connection;

import com.surrealdb.TestEnvVars;
import com.surrealdb.connection.exception.SurrealConnectionTimeoutException;
import com.surrealdb.connection.exception.SurrealNotConnectedException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Khalid Alharisi
 */
@Slf4j
@Testcontainers
public class SurrealConnectionTest {

    private static Optional<GenericContainer> container = Optional.empty();
    private static SurrealWebSocketConnection connection;

    @BeforeAll
    public static void setUp() {
        // Check if both host and port have been decalred as environment variable overrides
        Optional<String> envHost = Optional.ofNullable(System.getenv(TestEnvVars.SURREALDB_JAVA_HOST)).filter(str -> !str.isBlank());
        Optional<Integer> envPort = Optional.ofNullable(System.getenv(TestEnvVars.SURREALDB_JAVA_PORT)).map(strPort -> {
            try {
                return Integer.parseInt(strPort);
            } catch (NumberFormatException e) {
                return null;
            }
        });
        // if they have then use that address instead
        if (envHost.isPresent() && envPort.isPresent()) {
            connection = new SurrealWebSocketConnection(envHost.get(), envPort.get(), false);
        } else {
            // No env vars, start a container
            container = Optional.of(new GenericContainer(DockerImageName.parse("surrealdb/surrealdb:latest"))
                .withExposedPorts(8000).withCommand("start --log trace --user root --pass root memory"));
            container.get().start();
            connection = new SurrealWebSocketConnection(container.get().getHost(), container.get().getFirstMappedPort(), false);
        }
    }

    @AfterAll
    public static void teardown() {
        container.ifPresent(GenericContainer::stop);
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
