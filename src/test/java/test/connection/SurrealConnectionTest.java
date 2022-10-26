package test.connection;

import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.connection.SurrealConnectionSettings;
import com.surrealdb.connection.SurrealWebSocketConnection;
import com.surrealdb.connection.exception.SurrealConnectionTimeoutException;
import com.surrealdb.connection.exception.SurrealNotConnectedException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import test.TestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
            SurrealConnection connection = SurrealConnection.create("0.255.255.255", TestUtils.getPort(), TestUtils.useTls());
            connection.connect(3);
        });
    }

    @Test
    public void testHostNotReachable2() {
        assertThrows(SurrealConnectionTimeoutException.class, () -> {
            SurrealConnection connection = SurrealConnection.create(TestUtils.getHost(), 9999, TestUtils.useTls());
            connection.connect(3);
        });
    }

    @Test
    public void testInvalidHostname() {
        assertThrows(SurrealConnectionTimeoutException.class, () -> {
            SurrealConnection connection = SurrealConnection.create("some_hostname", TestUtils.getPort(), TestUtils.useTls());
            connection.connect(3);
        });
    }

    @Test
    public void testUserForgotToConnect() {
        assertThrows(SurrealNotConnectedException.class, () -> {
            SurrealConnection connection = SurrealConnection.create(TestUtils.getConnectionSettings());
            connection.rpc(null, "let", "some_key", "some_val");
        });
    }

    @Test
    public void testUserConnectsThenDisconnects() {
        assertThrows(SurrealNotConnectedException.class, () -> {
            SurrealConnection connection = SurrealConnection.create(TestUtils.getConnectionSettings());
            connection.connect(3);
            connection.disconnect();
            connection.rpc(null, "let", "some_key", "some_val");
        });
    }

    @Test
    void testAutoConnect() {
        assertDoesNotThrow(() -> {
            SurrealConnectionSettings settings = TestUtils.createConnectionSettingsBuilderWithDefaults()
                .setAutoConnect(true)
                .build();

            SurrealConnection connection = SurrealConnection.create(settings);
            // Normally, the user would have to call connect() to connect to the server.
            // However, since we set autoConnect to true, the connection will be established automatically.
            connection.rpc(null, "let", "some_key", "some_val");
        });
    }

}
