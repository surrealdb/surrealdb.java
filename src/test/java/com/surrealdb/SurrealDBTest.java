package com.surrealdb;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SurrealDBTest {

    @Test
    void surrealdb_websocket() throws SurrealDBException {
        try (Surreal surreal = new Surreal()) {
            // We expected an exception as there is no running server
            RuntimeException e = assertThrows(SurrealDBException.class, () -> {
                surreal.connect("ws://localhost:8000");
            });
            assertTrue(e.getMessage().startsWith("There was an error processing a remote WS request: IO error:"));
        }
    }

    @Test
    void surreal_db_memory() throws SurrealDBException {
        try (Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            try (Response response = surreal.query("INFO FOR ROOT;")) {
                Result result = response.take(0);
                assertTrue(result.hasNext());
                Value value = result.next();
                assertTrue(value.isArray());
            }
        }
    }
}
