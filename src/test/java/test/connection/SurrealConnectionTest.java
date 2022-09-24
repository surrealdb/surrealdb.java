package test.connection;

import com.surrealdb.connection.SurrealConnection;
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
            SurrealConnection connection = new SurrealWebSocketConnection(TestUtils.getHost(), TestUtils.getPort(), false);
            connection.connect(3);
        });
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
        assertThrows(SurrealNotConnectedException.class, () -> {
            SurrealConnection connection = new SurrealWebSocketConnection(TestUtils.getHost(), TestUtils.getPort(), false);
            connection.rpc(null, "let", "some_key", "some_val");
        });
    }

    @Test
    public void testUserConnectsThenDisconnects() {
        assertThrows(SurrealNotConnectedException.class, () -> {
            SurrealConnection connection = new SurrealWebSocketConnection(TestUtils.getHost(), TestUtils.getPort(), false);
            connection.connect(3);
            connection.disconnect();
            connection.rpc(null, "let", "some_key", "some_val");
        });
    }


}
