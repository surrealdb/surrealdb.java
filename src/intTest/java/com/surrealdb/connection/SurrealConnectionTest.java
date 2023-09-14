package com.surrealdb.connection;

import com.surrealdb.BaseIntegrationTest;
import com.surrealdb.connection.exception.SurrealConnectionTimeoutException;
import com.surrealdb.connection.exception.SurrealNotConnectedException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Khalid Alharisi
 */
@Slf4j
@Testcontainers
public class SurrealConnectionTest extends BaseIntegrationTest {

    @Test
    public void testConnectSuccessfully() {
        assertDoesNotThrow(() -> {
            final SurrealWebSocketConnection connection = new SurrealWebSocketConnection(testHost, testPort, false);
            connection.connect(3);
        });
    }

    @Test
    public void testHostNotReachable1() {
        assertThrows(SurrealConnectionTimeoutException.class, () -> {
            final SurrealConnection connection = new SurrealWebSocketConnection("172.18.0.10", 8000, false);
            connection.connect(3);
        });
    }

    @Test
    public void testHostNotReachable2() {
        assertThrows(SurrealConnectionTimeoutException.class, () -> {
            final SurrealConnection connection = new SurrealWebSocketConnection("localhost", 9999, false);
            connection.connect(3);
        });
    }

    @Test
    public void testInvalidHostname() {
        assertThrows(SurrealConnectionTimeoutException.class, () -> {
            final SurrealConnection connection = new SurrealWebSocketConnection("some_hostname", 8000, false);
            connection.connect(3);
        });
    }

    @Test
    public void testUserForgotToConnect() {
        assertThrows(SurrealNotConnectedException.class, () -> {
            final SurrealWebSocketConnection connection = new SurrealWebSocketConnection(testHost, testPort, false);
            connection.rpc(null, "let", "some_key", "some_val");
        });
    }

    @Test
    public void testUserConnectsThenDisconnects() {
        assertThrows(SurrealNotConnectedException.class, () -> {
            final SurrealWebSocketConnection connection = new SurrealWebSocketConnection(testHost, testPort, false);
            connection.connect(3);
            connection.disconnect();
            connection.rpc(null, "let", "some_key", "some_val");
        });
    }


}
