package com.surrealdb.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

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

	@Test
	void connectSurrealKV() throws SurrealException, IOException {
		// Use a directory under the current working directory instead of system temp.
		// On Windows CI, %TEMP% can trigger "Access is denied (os error 5)" due to
		// short paths, antivirus, or permissions; the project dir is writable.
		final Path dataDir = Paths.get(System.getProperty("user.dir"), "build", "surrealkv-it", "kv-" + System.nanoTime());
		Files.createDirectories(dataDir);
		try (final Surreal surreal = new Surreal()) {
			// Use forward slashes in the URL so the path is parsed correctly on all platforms (Windows
			// paths with backslashes can be misinterpreted in connection strings).
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
