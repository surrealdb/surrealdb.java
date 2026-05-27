package com.surrealdb.integration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import com.surrealdb.Surreal;
import com.surrealdb.SurrealException;
import com.surrealdb.signin.RootCredential;

public class IntegrationTest {

	// Skipped on Windows: SurrealDB is typically not started there in CI
	// (setup-surreal is Linux/macOS).
	@Test
	@EnabledOnOs({OS.LINUX, OS.MAC})
	void surrealdb_websocket() {
		// When SurrealDB is running at localhost:8000 (e.g. in CI), connect and verify.
		try (Surreal surreal = new Surreal()) {
			assertDoesNotThrow(() -> surreal.connect("ws://localhost:8000").useNs("test").useDb("test"));
		}
	}

	// TODO: SurrealKV file backend triggers "Access is denied (os error 5)" on
	// Windows CI
	// (both temp and project dir). Skip on Windows until root cause is fixed.
	@Test
	@EnabledOnOs({OS.LINUX, OS.MAC})
	void connectSurrealKV() throws SurrealException, IOException {
		final Path dataDir = Paths.get(System.getProperty("user.dir"), "build", "surrealkv-it",
				"kv-" + System.nanoTime());
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

	/**
	 * Regression test for <a href=
	 * "https://github.com/surrealdb/surrealdb.java/issues/160">#160</a>: looping
	 * useDb + health on a reused WebSocket connection used to balloon the SDK's
	 * per-session replay log and (on server 3.0.5) crash the server worker. A
	 * fixed driver finishes the loop quickly with stable per-iteration latency.
	 */
	@Test
	@EnabledOnOs({OS.LINUX, OS.MAC})
	void useDbHealthLoop_doesNotDegrade() {
		final int iterations = 200;
		final long budgetMs = 10_000;
		try (Surreal surreal = new Surreal()) {
			surreal.connect("ws://localhost:8000");
			surreal.signin(new RootCredential("root", "root"));
			surreal.useNs("issue_160_ns");
			long start = System.currentTimeMillis();
			for (int i = 0; i < iterations; i++) {
				surreal.useDb("issue_160_db");
				surreal.health();
			}
			long elapsed = System.currentTimeMillis() - start;
			assertTrue(elapsed < budgetMs,
					"useDb+health loop took " + elapsed + "ms (>" + budgetMs + "ms); replay log may be growing");
		}
	}
}
