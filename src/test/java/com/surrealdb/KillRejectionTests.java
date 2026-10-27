package com.surrealdb;

import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

/**
 * Regression tests: a KILL the server rejects must raise. The client used to
 * drop every KILL result, so killing an unknown or already-killed live query
 * silently succeeded.
 */
public class KillRejectionTests {

	private static final String SERVER_URL = System.getenv().getOrDefault("SURREALDB_URL", "ws://localhost:8000");

	private static void killReportsRejection(Surreal surreal) {
		surreal.query("DEFINE TABLE IF NOT EXISTS person");
		// An id that names no live query.
		final UUID unknown = UUID.randomUUID();
		// A query error naming the live query id, not an InternalException.
		final QueryException e = assertThrows(QueryException.class, () -> surreal.kill(unknown));
		assertTrue(e.getMessage().contains("Cannot execute KILL statement"), e.getMessage());
		assertTrue(e.getMessage().contains(unknown.toString()), e.getMessage());
		// A real live query can be killed once; the second kill is rejected.
		try (final LiveStream stream = surreal.selectLive("person")) {
			assertDoesNotThrow(() -> surreal.kill(stream.getQueryId()));
			assertThrows(QueryException.class, () -> surreal.kill(stream.getQueryId()));
		}
	}

	private static void withServer(Consumer<Surreal> test) {
		try (final Surreal surreal = new Surreal()) {
			try {
				surreal.connect(SERVER_URL);
			} catch (SurrealException e) {
				Assumptions.abort("No SurrealDB server at " + SERVER_URL);
			}
			surreal.useNs("test_ns").useDb("kill_" + System.nanoTime());
			test.accept(surreal);
		}
	}

	@Test
	void killReportsRejectionEmbedded() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			killReportsRejection(surreal);
		}
	}

	@Test
	void killReportsRejectionRemote() {
		withServer(KillRejectionTests::killReportsRejection);
	}
}
