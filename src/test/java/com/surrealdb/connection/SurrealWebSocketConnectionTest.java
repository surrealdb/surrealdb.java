package com.surrealdb.connection;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
}
