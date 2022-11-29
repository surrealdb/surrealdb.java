package meta.tests;

import com.surrealdb.client.SurrealBiDirectionalClient;
import com.surrealdb.client.SurrealClientSettings;
import com.surrealdb.exception.SurrealConnectionTimeoutException;
import com.surrealdb.exception.SurrealNotConnectedException;
import lombok.val;
import meta.utils.TestUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("unused")
public abstract class SurrealBiDirectionalClientTests {

    protected abstract @NotNull SurrealBiDirectionalClient createClient(@NotNull SurrealClientSettings settings);

    @Test
    public void testConnectSuccessfully() {
        SurrealBiDirectionalClient client = createClient(TestUtils.getClientSettings());

        assertDoesNotThrow(() -> client.connect(3, TimeUnit.SECONDS));
    }

    @Test
    public void testHostNotReachable() {
        SurrealClientSettings settings = TestUtils.createClientSettingsBuilderWithDefaults()
            .setUriFromComponents(TestUtils.getProtocol(), "0.255.255.255", TestUtils.getPort())
            .build();
        SurrealBiDirectionalClient client = createClient(settings);

        assertThrows(SurrealConnectionTimeoutException.class, () -> client.connect(3, TimeUnit.SECONDS));
    }

    @Test
    public void testPortNotReachable() {
        SurrealClientSettings settings = TestUtils.createClientSettingsBuilderWithDefaults()
            .setUriFromComponents(TestUtils.getProtocol(), TestUtils.getHost(), 9999)
            .build();
        SurrealBiDirectionalClient client = createClient(settings);

        assertThrows(SurrealConnectionTimeoutException.class, () -> client.connect(3, TimeUnit.SECONDS));
    }

    @Test
    public void testInvalidHostname() {
        SurrealClientSettings settings = TestUtils.createClientSettingsBuilderWithDefaults()
            .setUriFromComponents(TestUtils.getProtocol(), "some_hostname", TestUtils.getPort())
            .build();
        SurrealBiDirectionalClient client = createClient(settings);

        assertThrows(SurrealConnectionTimeoutException.class, () -> client.connect(3, TimeUnit.SECONDS));
    }

    @Test
    public void testUserForgotToConnect() {
        SurrealBiDirectionalClient client = createClient(TestUtils.getClientSettings());

        assertThrows(SurrealNotConnectedException.class, client::ping);
    }

    @Test
    public void testUserConnectsThenDisconnects() {
        SurrealBiDirectionalClient client = createClient(TestUtils.getClientSettings());

        client.connect(3, TimeUnit.SECONDS);

        assertTrue(client.isConnected());
        assertDoesNotThrow(client::ping);

        client.disconnect();

        assertFalse(client.isConnected());
        assertThrows(SurrealNotConnectedException.class, client::ping);
    }

    @Test
    void testConnectThenDisconnectThenConnect() {
        SurrealBiDirectionalClient client = createClient(TestUtils.getClientSettings());

        client.connect(3, TimeUnit.SECONDS);
        assertTrue(client.isConnected());
        client.disconnect();
        assertFalse(client.isConnected());
        client.connect(3, TimeUnit.SECONDS);

        assertTrue(client.isConnected());
        assertDoesNotThrow(client::ping);
    }

    @Test
    void testDoubleConnect() {
        SurrealBiDirectionalClient client = createClient(TestUtils.getClientSettings());

        client.connect(3, TimeUnit.SECONDS);

        assertDoesNotThrow(() -> client.connect(3, TimeUnit.SECONDS));
        assertDoesNotThrow(client::ping);
    }

    @Test
    void testIsConnected() {
        SurrealBiDirectionalClient client = createClient(TestUtils.getClientSettings());

        // Verify that the connection is not connected.
        assertFalse(client.isConnected());
        // Connect to the server.
        client.connect(3, TimeUnit.SECONDS);
        // Verify that the connection is connected.
        assertDoesNotThrow(client::ping);
        assertTrue(client.isConnected());
        // Disconnect from the server.
        client.disconnect();
        // Verify that the connection is not connected.
        assertThrows(SurrealNotConnectedException.class, client::ping);
//        assertFalse(client.isConnected());
    }

    @Test
    @Timeout(value = 10_000, unit = TimeUnit.MILLISECONDS)
    void testHighVolumeConcurrentTraffic() {
        SurrealBiDirectionalClient client = createClient(TestUtils.getClientSettings());
        client.connect(3, TimeUnit.SECONDS);

        CompletableFuture<?>[] pings = IntStream.range(0, 1000)
            .parallel()
            .mapToObj(i -> client.pingAsync())
            .toArray(CompletableFuture[]::new);

        CompletableFuture.allOf(pings).join();
    }
}
