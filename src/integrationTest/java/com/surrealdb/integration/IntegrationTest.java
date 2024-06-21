package com.surrealdb.integration;

import com.surrealdb.Surreal;
import com.surrealdb.SurrealDBException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IntegrationTest {

    @Test
    void surrealdb_websocket() throws SurrealDBException {
        try (Surreal surreal = new Surreal()) {
            // We expected an exception as there is no running server
            RuntimeException e = assertThrows(RuntimeException.class, () -> {
                surreal.connect("ws://localhost:8000");
            });
            assertTrue(e.getMessage().startsWith("There was an error processing a remote WS request: IO error:"), e::getMessage);
        }
    }

    @Test
    void surreal_db_memory() throws SurrealDBException {
        try (Surreal surreal = new Surreal()) {
            surreal.connect("memory");
        }
    }
}
