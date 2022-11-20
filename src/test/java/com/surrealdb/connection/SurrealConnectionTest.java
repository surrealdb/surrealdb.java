package com.surrealdb.connection;

import com.surrealdb.connection.exception.SurrealConnectionTimeoutException;
import com.surrealdb.connection.exception.SurrealNotConnectedException;
import com.surrealdb.meta.utils.TestUtils;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Khalid Alharisi
 */
public class SurrealConnectionTest {

    @Test
    public void testConnectSuccessfully() {
        val connection = SurrealConnection.create(TestUtils.getConnectionSettings());

        assertDoesNotThrow(() -> connection.connect(3));
    }

    @Test
    public void testHostNotReachable() {
        val connection = SurrealConnection.create(TestUtils.getProtocol(), "0.255.255.255", TestUtils.getPort());

        assertThrows(SurrealConnectionTimeoutException.class, () -> connection.connect(3));
    }

    @Test
    public void testPortNotReachable() {
        val connection = SurrealConnection.create(TestUtils.getProtocol(), TestUtils.getHost(), 9999);

        assertThrows(SurrealConnectionTimeoutException.class, () -> connection.connect(3));
    }

    @Test
    public void testInvalidHostname() {
        val connection = SurrealConnection.create(TestUtils.getProtocol(), "some_hostname", TestUtils.getPort());

        assertThrows(SurrealConnectionTimeoutException.class, () -> connection.connect(3));
    }

    @Test
    public void testUserForgotToConnect() {
        val connection = SurrealConnection.create(TestUtils.getConnectionSettings());

        assertThrows(SurrealNotConnectedException.class, () -> getCallbackResults(connection.rpc("ping")));
    }

    @Test
    public void testUserConnectsThenDisconnects() {
        val connection = SurrealConnection.create(TestUtils.getConnectionSettings());

        connection.connect(3);

        assertTrue(connection.isConnected());
        assertDoesNotThrow(() -> getCallbackResults(connection.rpc("ping")));

        connection.disconnect();

        assertFalse(connection.isConnected());
        assertThrows(SurrealNotConnectedException.class, () -> getCallbackResults(connection.rpc("ping")));
    }

    @Test
    void testConnectThenDisconnectThenConnect() {
        val connection = SurrealConnection.create(TestUtils.getConnectionSettings());

        connection.connect(3);
        connection.disconnect();
        connection.connect(3);

        assertTrue(connection.isConnected());
        assertDoesNotThrow(() -> getCallbackResults(connection.rpc("ping")));
    }

    @Test
    void testDoubleConnect() {
        val connection = SurrealConnection.create(TestUtils.getConnectionSettings());

        connection.connect(3);

        assertDoesNotThrow(() -> connection.connect(3));
        assertDoesNotThrow(() -> getCallbackResults(connection.rpc("ping")));
    }

    @Test
    void testAutoConnect() {
        val settings = TestUtils.createConnectionSettingsBuilderWithDefaults().setAutoConnect(true).build();
        val connection = SurrealConnection.create(settings);
        // Normally, the user would have to call connect() to connect to the server.
        // However, since we set autoConnect to true, the connection will be established automatically.
        assertTrue(connection.isConnected());
        assertDoesNotThrow(() -> getCallbackResults(connection.rpc("ping")));
    }

    @Test
    void testIsConnected() {
        val connection = SurrealConnection.create(TestUtils.getConnectionSettings());

        // Verify that the connection is not connected.
        assertFalse(connection.isConnected());
        // Connect to the server.
        connection.connect(3);
        // Verify that the connection is connected.
        assertDoesNotThrow(() -> getCallbackResults(connection.rpc("ping")));
        assertTrue(connection.isConnected());
        // Disconnect from the server.
        connection.disconnect();
        // Verify that the connection is not connected.
        assertThrows(SurrealNotConnectedException.class, () -> getCallbackResults(connection.rpc("ping")));
        assertFalse(connection.isConnected());
    }

    @Test
    @Timeout(value = 10_000, unit = TimeUnit.MILLISECONDS)
    void testHighVolumeConcurrentTraffic() {
        val connection = SurrealConnection.create(TestUtils.getConnectionSettings());
        connection.connect(3);

        val pings = IntStream.range(0, 1000)
            .parallel()
            .mapToObj(i -> connection.rpc("ping"))
            .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(pings).join();
    }

    private <T> T getCallbackResults(CompletableFuture<T> future) throws Throwable {
        try {
            return future.join();
        } catch (CompletionException completionException) {
            throw completionException.getCause();
        }
    }
}
