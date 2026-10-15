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
 * When {@code SURREALDB_URL} is not set, tests that need a server skip
 * themselves if none is reachable, so a local {@code ./gradlew test} without
 * one stays green. When it is set (as in CI), the server was asked for
 * explicitly and failing to reach it fails the test.
 */
public final class TestServer {

	/** WebSocket endpoint of the test server. */
	public static final String WS_URL = System.getenv().getOrDefault("SURREALDB_URL", "ws://localhost:8000");

	/** Whether a server was configured explicitly, so it must be reachable. */
	private static final boolean REQUIRED = System.getenv("SURREALDB_URL") != null;

	/** HTTP endpoint of the same server. */
	public static final String HTTP_URL = WS_URL.replaceFirst("^ws", "http");

	private static final int CONNECT_TIMEOUT_SECONDS = 10;

	private TestServer() {
	}

	/**
	 * Connects to {@code url}, signs in as root and selects a fresh database. If
	 * the server cannot be reached or signed in to in time, fails the calling test
	 * when {@code SURREALDB_URL} is set, and aborts (skips) it otherwise.
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
			if (REQUIRED) {
				throw new IllegalStateException(
						"Cannot use the SurrealDB server at " + url + " (configured by SURREALDB_URL)", e);
			}
			Assumptions.abort("No SurrealDB server reachable at " + url + " (" + e + ")");
		} finally {
			executor.shutdownNow();
		}
		surreal.useNs("java_tests").useDb("db_" + Long.toHexString(System.nanoTime()));
		return surreal;
	}
}
