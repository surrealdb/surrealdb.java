package com.surrealdb;

import com.surrealdb.pojos.Person;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ConnectionTests {

    @Test
    void connectWebsocket() throws SurrealException {
        try (Surreal surreal = new Surreal()) {
            // We expected an exception as there is no running server
            RuntimeException e = assertThrows(SurrealException.class, () -> {
                surreal.connect("ws://localhost:8000");
            });
            assertTrue(e.getMessage().startsWith("There was an error processing a remote WS request: IO error:"));
        }
    }

    @Test
    void connectSurrealKV() throws SurrealException, IOException {
        final Path tempDir = Files.createTempDirectory("surrealkv");
        try (final Surreal surreal = new Surreal()) {
            surreal.connect("surrealkv://" + tempDir.toAbsolutePath()).useNs("test").useDb("test");
            final Person created = surreal.create(Person.class, "person", Helpers.tobie);
            assertNotNull(created.id);
        }
    }

    @Test
    void connectMemory() throws SurrealException {
        try (final Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            {
                final Response response = surreal.query("INFO FOR ROOT");
                final Value value = response.take(0);
                assertTrue(value.isObject());
                final Object object = value.getObject();
                assertEquals(object.len(), 2);
                {
                    final Value ns = object.get("namespaces");
                    assertTrue(ns.isObject());
                    assertEquals("{  }", ns.toString());
                    assertEquals(ns.getObject().len(), 0);
                }
                {
                    final Value users = object.get("users");
                    assertTrue(users.isObject());
                    assertEquals(users.getObject().len(), 0);
                    assertEquals("{}", users.toPrettyString());
                }
            }
        }
    }
}
