package test.connection;

import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.connection.exception.SurrealConnectionTimeoutException;
import com.surrealdb.connection.exception.SurrealException;
import com.surrealdb.connection.exception.SurrealNotConnectedException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import test.TestUtils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Khalid Alharisi
 */
@Slf4j
public class SurrealConnectionTest {

    @Test
    public void testConnectSuccessfully() {
        val connection = SurrealConnection.create(TestUtils.getConnectionSettings());

        assertDoesNotThrow(() -> connection.connect(3));
    }

    @Test
    public void testHostNotReachable1() {
        val connection = SurrealConnection.create(TestUtils.getProtocol(), "0.255.255.255", TestUtils.getPort());

        assertThrows(SurrealConnectionTimeoutException.class, () -> connection.connect(3));
    }

    @Test
    public void testHostNotReachable2() {
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

        val callback = connection.rpc("let", "some_key", "some_val");
        assertThrows(SurrealNotConnectedException.class, () -> getCallbackResults(callback));
    }

    @Test
    public void testUserConnectsThenDisconnects() {
        val connection = SurrealConnection.create(TestUtils.getConnectionSettings());

        connection.connect(3);
        assertTrue(connection.isConnected());
        connection.disconnect();

        val callback = connection.rpc("let", "some_key", "some_val");
        assertThrows(SurrealNotConnectedException.class, () -> getCallbackResults(callback));
    }

    @Test
    void testAutoConnect() {
        val settings = TestUtils.createConnectionSettingsBuilderWithDefaults().setAutoConnect(true).build();
        val connection = SurrealConnection.create(settings);
        // Normally, the user would have to call connect() to connect to the server.
        // However, since we set autoConnect to true, the connection will be established automatically.
        val callback = connection.rpc("let", "some_key", "some_val");
        assertDoesNotThrow(() -> getCallbackResults(callback));
    }

    @Test
    void testIsConnected() {
        val connection = SurrealConnection.create(TestUtils.getConnectionSettings());

        // Verify that the connection is not connected.
        assertFalse(connection.isConnected());
        // Connect to the server.
        connection.connect(3);
        // Verify that the connection is connected.
        assertDoesNotThrow(() -> getCallbackResults(connection.rpc("let", "some_key", "some_val")));
        assertTrue(connection.isConnected());
        // Disconnect from the server.
        connection.disconnect();
        // Verify that the connection is not connected.
        assertThrows(SurrealNotConnectedException.class, () -> getCallbackResults(connection.rpc("let", "some_key", "some_val")));
        assertFalse(connection.isConnected());
    }

    private <T> T getCallbackResults(CompletableFuture<T> future) {
        try {
            return future.join();
        } catch (CompletionException completionException) {
            throw (SurrealException) completionException.getCause();
        }
    }
}
