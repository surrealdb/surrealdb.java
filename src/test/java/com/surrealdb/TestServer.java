package com.surrealdb;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Assumptions;

import com.surrealdb.signin.RootCredential;

/**
 * The SurrealDB server that server-backed tests run against. Set
 * {@code SURREALDB_URL} to its WebSocket endpoint (default
 * {@code ws://localhost:8000}); the server is expected to have a root user
 * {@code root}/{@code root}, as started by:
 *
 * <pre>{@code
 * surreal start -u root -p root --unauthenticated --allow-all --bind 127.0.0.1:8000 memory
 * }</pre>
 *
 * Tests that need a server skip themselves when none is reachable, so a local
 * {@code ./gradlew test} without one stays green.
 */
public final class TestServer {

	/** WebSocket endpoint of the test server. */
	public static final String WS_URL = System.getenv().getOrDefault("SURREALDB_URL", "ws://localhost:8000");

	/** HTTP endpoint of the same server. */
	public static final String HTTP_URL = WS_URL.replaceFirst("^ws", "http");

	private static final int CONNECT_TIMEOUT_SECONDS = 10;

	private TestServer() {
	}

	/**
	 * Connects to {@code url}, signs in as root and selects a fresh database, or
	 * aborts (skips) the calling test if the server cannot be reached in time.
	 */
	public static Surreal connect(String url) {
		final Surreal surreal = new Surreal();
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			final Future<?> f = executor.submit(() -> {
				surreal.connect(url);
				surreal.signin(new RootCredential("root", "root"));
			});
			f.get(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (Exception e) {
			surreal.close();
			Assumptions.abort("No SurrealDB server reachable at " + url + " (" + e + ")");
		} finally {
			executor.shutdownNow();
		}
		surreal.useNs("java_tests").useDb("db_" + Long.toHexString(System.nanoTime()));
		return surreal;
	}
}
