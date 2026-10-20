package com.surrealdb;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import com.surrealdb.signin.RootCredential;

/**
 * The RocksDB storage engine and the gRPC protocol, both enabled by the crate's
 * kv-rocksdb and protocol-grpc features.
 */
public class EngineFeaturesTests {

	private static final String SERVER_URL = System.getenv().getOrDefault("SURREALDB_URL", "ws://localhost:8000");

	@Test
	void rocksdbPersistsAcrossConnections() throws IOException {
		// Not @TempDir: JUnit deletes that straight after the test, while the engine
		// may still be closing RocksDB (see below), which fails the test.
		final Path dir = Files.createTempDirectory("surrealdb-rocksdb");
		try {
			reopen("rocksdb://" + dir.resolve("db").toAbsolutePath());
		} finally {
			deleteEventually(dir);
		}
	}

	private static void reopen(String url) {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect(url).useNs("test_ns").useDb("test_db");
			surreal.query("CREATE kept:1 SET v = 42");
		}
		// close() returns before the embedded engine has released RocksDB's lock, so
		// reopening straight away can fail with "lock hold by current process" for
		// a few hundred milliseconds.
		final long deadline = System.currentTimeMillis() + 10_000;
		while (true) {
			try (final Surreal surreal = new Surreal()) {
				surreal.connect(url).useNs("test_ns").useDb("test_db");
				assertEquals(42L, surreal.query("SELECT VALUE v FROM ONLY kept:1").take(0).getLong());
				return;
			} catch (SurrealException e) {
				if (System.currentTimeMillis() > deadline) {
					throw e;
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					throw e;
				}
			}
		}
	}

	private static void deleteEventually(Path dir) {
		final long deadline = System.currentTimeMillis() + 10_000;
		while (true) {
			try (Stream<Path> paths = Files.walk(dir)) {
				final List<Path> all = paths.sorted(Comparator.reverseOrder()).collect(Collectors.toList());
				for (final Path p : all) {
					Files.deleteIfExists(p);
				}
				return;
			} catch (IOException | UncheckedIOException e) {
				if (System.currentTimeMillis() > deadline) {
					return; // best effort: it is a temp directory
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					return;
				}
			}
		}
	}

	@Test
	void grpcRoundTrip() {
		// gRPC is served on the same port as HTTP and WebSocket, from 3.4.
		final String version;
		try (final Surreal ws = new Surreal()) {
			try {
				ws.connect(SERVER_URL);
			} catch (SurrealException e) {
				Assumptions.abort("No SurrealDB server at " + SERVER_URL);
				return;
			}
			version = ws.version();
		}
		final String[] parts = version.replaceFirst("^surrealdb-", "").split("[.-]");
		final int major = Integer.parseInt(parts[0]);
		final int minor = Integer.parseInt(parts[1]);
		Assumptions.assumeTrue(major > 3 || (major == 3 && minor >= 4), "gRPC needs server 3.4+, got " + version);

		try (final Surreal surreal = new Surreal()) {
			surreal.connect(SERVER_URL.replaceFirst("^wss?", "grpc"));
			surreal.signin(new RootCredential("root", "root"));
			surreal.useNs("test_ns").useDb("grpc_" + System.nanoTime());
			surreal.query("CREATE item:1 SET v = 'grpc'");
			assertEquals("grpc", surreal.query("SELECT VALUE v FROM ONLY item:1").take(0).getString());
			assertTrue(surreal.health());
		}
	}
}
