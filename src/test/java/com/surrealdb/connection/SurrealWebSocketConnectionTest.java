package com.surrealdb.connection;

import com.surrealdb.driver.SyncSurrealDriver;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
class SurrealWebSocketConnectionTest {

    @DisplayName("Can create a Connection")
    @ParameterizedTest(name = "Can create a Connection for {3}")
    @MethodSource("urlProvider")
    public void canCreateAConnectionWithAppropriateUrl(String host, int port, boolean useTls, String expectedUrl) {
        SurrealWebSocketConnection connection = new SurrealWebSocketConnection(host, port, useTls);
        assertEquals(expectedUrl, connection.getURI().toASCIIString());
    }

    static Stream<Arguments> urlProvider() {
        return Stream.of(
            Arguments.of("1", 3, false, "ws://1:3/rpc"),
            Arguments.of("10", 20, true, "wss://10:20/rpc")
        );
    }

    private static final String localAddr = "ws://localhost";
    private static final int localPort = 8000;

    @Test
    public void canHandleSingleObjectResult() {
        SurrealWebSocketConnection connection = new SurrealWebSocketConnection(localAddr, localPort, false);
        connection.connect(1);
        SyncSurrealDriver driver = new SyncSurrealDriver(connection);
        Map<String, String> leslie = Map.of(
            "name" , "Leslie",
            "surname","Lamport"
        );
        driver.create("person:leslie", leslie );
        List<Map> vals = driver.select("person:leslie", Map.class);
        assertEquals(1, vals.size());
    }

    @Test
    void canHandleMultiObjectResult() {
        SurrealWebSocketConnection connection = new SurrealWebSocketConnection(localAddr, localPort, false);
        connection.connect(1);
        SyncSurrealDriver driver = new SyncSurrealDriver(connection);
        Map<String, String> leslie = Map.of(
            "name" , "Leslie",
            "surname","Lamport"
        );
        driver.create("person:leslie", leslie );
        Map<String,String> barbara = Map.of(
            "name", "Barbara",
            "surname", "Liskov"
        );
        List<Map> vals = driver.select("person", Map.class);
        assertEquals(2, vals.size());
        Assertions.fail("expected");
    }
}
