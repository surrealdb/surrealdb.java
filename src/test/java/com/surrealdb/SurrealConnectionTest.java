package com.surrealdb;

import com.surrealdb.WebSocketSurrealClient;
import com.surrealdb.exception.SurrealConnectionTimeoutException;
import com.surrealdb.exception.SurrealNotConnectedException;
import meta.utils.TestUtils;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Khalid Alharisi
 */
public class SurrealConnectionTest {

    @Test
    public void testConnectSuccessfully() {
        val client = WebSocketSurrealClient.create(TestUtils.getClientSettings());

        assertDoesNotThrow(() -> client.connect(3, TimeUnit.SECONDS));
    }

    @Test
    public void testHostNotReachable() {
        val client = WebSocketSurrealClient.create(TestUtils.getProtocol(), "0.255.255.255", TestUtils.getPort());

        assertThrows(SurrealConnectionTimeoutException.class, () -> client.connect(3, TimeUnit.SECONDS));
    }

    @Test
    public void testPortNotReachable() {
        val client = WebSocketSurrealClient.create(TestUtils.getProtocol(), TestUtils.getHost(), 9999);

        assertThrows(SurrealConnectionTimeoutException.class, () -> client.connect(3, TimeUnit.SECONDS));
    }

    @Test
    public void testInvalidHostname() {
        val client = WebSocketSurrealClient.create(TestUtils.getProtocol(), "some_hostname", TestUtils.getPort());

        assertThrows(SurrealConnectionTimeoutException.class, () -> client.connect(3, TimeUnit.SECONDS));
    }

    @Test
    public void testUserForgotToConnect() {
        val client = WebSocketSurrealClient.create(TestUtils.getClientSettings());

        assertThrows(SurrealNotConnectedException.class, client::ping);
    }

    @Test
    public void testUserConnectsThenDisconnects() {
        val client = WebSocketSurrealClient.create(TestUtils.getClientSettings());

        client.connect(3, TimeUnit.SECONDS);

        // assertTrue(client.isConnected());
        assertDoesNotThrow(client::ping);

        client.disconnect();

        // assertFalse(client.isConnected());
        assertThrows(SurrealNotConnectedException.class, client::ping);
    }

    @Test
    void testConnectThenDisconnectThenConnect() {
        val client = WebSocketSurrealClient.create(TestUtils.getClientSettings());

        client.connect(3, TimeUnit.SECONDS);
        client.disconnect();
        client.connect(3, TimeUnit.SECONDS);

//        assertTrue(connection.isConnected());
        assertDoesNotThrow(client::ping);
    }

    @Test
    void testDoubleConnect() {
        val client = WebSocketSurrealClient.create(TestUtils.getClientSettings());

        client.connect(3, TimeUnit.SECONDS);

        assertDoesNotThrow(() -> client.connect(3, TimeUnit.SECONDS));
        assertDoesNotThrow(client::ping);
    }

    @Test
    void testIsConnected() {
        val client = WebSocketSurrealClient.create(TestUtils.getClientSettings());

        // Verify that the connection is not connected.
//        assertFalse(connection.isConnected());
        // Connect to the server.
        client.connect(3, TimeUnit.SECONDS);
        // Verify that the connection is connected.
        assertDoesNotThrow(client::ping);
//        assertTrue(client.isConnected());
        // Disconnect from the server.
        client.disconnect();
        // Verify that the connection is not connected.
        assertThrows(SurrealNotConnectedException.class, client::ping);
//        assertFalse(client.isConnected());
    }

    @Test
    @Timeout(value = 10_000, unit = TimeUnit.MILLISECONDS)
    void testHighVolumeConcurrentTraffic() {
        val client = WebSocketSurrealClient.create(TestUtils.getClientSettings());
        client.connect(3, TimeUnit.SECONDS);

        val pings = IntStream.range(0, 1000)
            .parallel()
            .mapToObj(i -> client.pingAsync())
            .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(pings).join();
    }
}
