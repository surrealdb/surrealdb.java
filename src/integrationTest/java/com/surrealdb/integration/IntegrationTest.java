package com.surrealdb.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import com.surrealdb.Surreal;
import com.surrealdb.SurrealException;

public class IntegrationTest {

	@Test
	void surrealdb_websocket() throws SurrealException {
		try (Surreal surreal = new Surreal()) {
			// We expected an exception as there is no running server
			RuntimeException e = assertThrows(RuntimeException.class, () -> {
				surreal.connect("ws://localhost:8000");
			});
			assertTrue(e.getMessage().startsWith("There was an error processing a WebSocket request: IO error"),
					e::getMessage);
		}
	}

	// TODO: SurrealKV file backend triggers "Access is denied (os error 5)" on Windows CI
	// (both temp and project dir). Skip on Windows until root cause is fixed.
	@Test
	@EnabledOnOs({ OS.LINUX, OS.MAC })
	void connectSurrealKV() throws SurrealException, IOException {
		final Path dataDir = Paths.get(System.getProperty("user.dir"), "build", "surrealkv-it", "kv-" + System.nanoTime());
		Files.createDirectories(dataDir);
		try (final Surreal surreal = new Surreal()) {
			String path = dataDir.toAbsolutePath().toString().replace('\\', '/');
			surreal.connect("surrealkv://" + path).useNs("test").useDb("test");
		}
	}

	@Test
	void surreal_db_memory() throws SurrealException {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory");
		}
	}
}
