package com.surrealdb.integration;

import com.surrealdb.Surreal;
import com.surrealdb.SurrealException;
import com.surrealdb.signin.Root;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class IntegrationTest {

    @Test
    void surrealdb_websocket() throws SurrealException {
        try (Surreal surreal = new Surreal()) {
            // We expected an exception as there is no running server
            RuntimeException e = assertThrows(RuntimeException.class, () -> {
                surreal.connect("ws://localhost:8000");
            });
            assertTrue(e.getMessage().startsWith("There was an error processing a remote WS request: IO error:"), e::getMessage);
        }
    }

    @Test
    void connectSurrealKV() throws SurrealException, IOException {
        final Path tempDir = Files.createTempDirectory("surrealkv");
        try (final Surreal surreal = new Surreal()) {
            surreal.connect("surrealkv://" + tempDir.toAbsolutePath()).useNs("test").useDb("test");
            surreal.signin(new Root("test", "test"));
        }
    }

    @Test
    void surreal_db_memory() throws SurrealException {
        try (Surreal surreal = new Surreal()) {
            surreal.connect("memory");
        }
    }
}
