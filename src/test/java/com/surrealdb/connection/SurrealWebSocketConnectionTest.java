package com.surrealdb.connection;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.surrealdb.driver.SyncSurrealDriver;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
class SurrealWebSocketConnectionTest {

    private static final String localAddr = "ws://localhost";
    private static final int localPort = 8000;

    static Stream<Arguments> urlProvider() {
        return Stream.of(
                Arguments.of("1", 3, false, "ws://1:3/rpc"),
                Arguments.of("10", 20, true, "wss://10:20/rpc"));
    }

    @DisplayName("Can create a Connection")
    @ParameterizedTest(name = "Can create a Connection for {3}")
    @MethodSource("urlProvider")
    public void canCreateAConnectionWithAppropriateUrl(
            final String host, final int port, final boolean useTls, final String expectedUrl) {
        final SurrealWebSocketConnection connection =
                new SurrealWebSocketConnection(host, port, useTls);
        assertEquals(expectedUrl, connection.getURI().toASCIIString());
    }

    @Test
    public void canHandleSingleObjectResult() {
        final SurrealWebSocketConnection connection =
                new SurrealWebSocketConnection(localAddr, localPort, false);
        connection.connect(1);
        final SyncSurrealDriver driver = new SyncSurrealDriver(connection);
        final Map<String, String> leslie =
                Map.of(
                        "name", "Leslie",
                        "surname", "Lamport");
        driver.create("person:leslie", leslie);
        final List<Map> vals = driver.select("person:leslie", Map.class);
        assertEquals(1, vals.size());
    }

    @Test
    void canHandleMultiObjectResult() {
        final SurrealWebSocketConnection connection =
                new SurrealWebSocketConnection(localAddr, localPort, false);
        connection.connect(1);
        final SyncSurrealDriver driver = new SyncSurrealDriver(connection);
        final Map<String, String> leslie =
                Map.of(
                        "name", "Leslie",
                        "surname", "Lamport");
        driver.create("person:leslie", leslie);
        final Map<String, String> barbara =
                Map.of(
                        "name", "Barbara",
                        "surname", "Liskov");
        final List<Map> vals = driver.select("person", Map.class);
        assertEquals(2, vals.size());
        Assertions.fail("expected");
    }
}
