package com.surrealdb.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import com.surrealdb.Surreal;
import com.surrealdb.SurrealException;

public class IntegrationTest {

	// Skipped on Windows: SurrealDB is typically not started there in CI (setup-surreal is Linux/macOS).
	@Test
	@EnabledOnOs({ OS.LINUX, OS.MAC })
	void surrealdb_websocket() {
		// When SurrealDB is running at localhost:8000 (e.g. in CI), connect and verify.
		try (Surreal surreal = new Surreal()) {
			assertDoesNotThrow(() -> surreal.connect("ws://localhost:8000").useNs("test").useDb("test"));
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
