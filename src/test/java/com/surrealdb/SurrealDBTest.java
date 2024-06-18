package com.surrealdb;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SurrealDBTest {

    @Test
    void surrealdb_websocket() throws Exception {
        try (Surreal surreal = Surreal.new_instance()) {
            // We expected an exception as there is no running server
            RuntimeException e = assertThrows(RuntimeException.class, () -> {
                surreal.connect("ws://localhost:8000");
            });
            assertTrue(e.getMessage().startsWith("There was an error processing a remote WS request: IO error: Connection refused "));
        }
    }

    @Test
    void surreal_db_memory() throws Exception {
        try (Surreal surreal = Surreal.new_instance()) {
            surreal.connect("memory");
        }
    }
}
