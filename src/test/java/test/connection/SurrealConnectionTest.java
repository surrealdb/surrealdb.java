package test.connection;

import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.connection.exception.SurrealConnectionTimeoutException;
import com.surrealdb.connection.exception.SurrealNotConnectedException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import test.TestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Khalid Alharisi
 */
@Slf4j
public class SurrealConnectionTest {

    @Test
    public void testConnectSuccessfully() {
        assertDoesNotThrow(() -> {
            SurrealConnection connection = SurrealConnection.create(TestUtils.getConnectionSettings());
            connection.connect(3);
        });
    }

    @Test
    public void testHostNotReachable1() {
        assertThrows(SurrealConnectionTimeoutException.class, () -> {
            SurrealConnection connection = SurrealConnection.create(TestUtils.getProtocol(), "0.255.255.255", TestUtils.getPort());
            connection.connect(3);
        });
    }

    @Test
    public void testHostNotReachable2() {
        assertThrows(SurrealConnectionTimeoutException.class, () -> {
            SurrealConnection connection = SurrealConnection.create(TestUtils.getProtocol(), TestUtils.getHost(), 9999);
            connection.connect(3);
        });
    }

    @Test
    public void testInvalidHostname() {
        assertThrows(SurrealConnectionTimeoutException.class, () -> {
            SurrealConnection connection = SurrealConnection.create(TestUtils.getProtocol(), "some_hostname", TestUtils.getPort());
            connection.connect(3);
        });
    }

    @Test
    public void testUserForgotToConnect() {
        assertThrows(SurrealNotConnectedException.class, () -> {
            SurrealConnection connection = SurrealConnection.create(TestUtils.getConnectionSettings());
            connection.rpc("let", "some_key", "some_val");
        });
    }

    @Test
    public void testUserConnectsThenDisconnects() {
        assertThrows(SurrealNotConnectedException.class, () -> {
            SurrealConnection connection = SurrealConnection.create(TestUtils.getConnectionSettings());
            connection.connect(3);
            assertTrue(connection.isConnected());
            connection.disconnect();
            connection.rpc("let", "some_key", "some_val");
        });
    }

    @Test
    void testAutoConnect() {
        assertDoesNotThrow(() -> {
            val settings = TestUtils.createConnectionSettingsBuilderWithDefaults().setAutoConnect(true).build();
            val connection = SurrealConnection.create(settings);
            // Normally, the user would have to call connect() to connect to the server.
            // However, since we set autoConnect to true, the connection will be established automatically.
            connection.rpc("let", "some_key", "some_val");
        });
    }

    @Test
    void testIsConnected() {
        val connection = SurrealConnection.create(TestUtils.getConnectionSettings());

        assertFalse(connection.isConnected());
        connection.connect(3);
        assertTrue(connection.isConnected());
        connection.disconnect();
        assertFalse(connection.isConnected());
    }

}
