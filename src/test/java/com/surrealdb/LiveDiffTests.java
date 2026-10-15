package com.surrealdb;

import java.util.Optional;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import com.surrealdb.signin.RootCredential;

/**
 * Live queries in diff mode ({@code LIVE SELECT DIFF}), where notifications
 * carry JSON Patch operations instead of whole records.
 */
public class LiveDiffTests {

	private static final String SERVER_URL = System.getenv().getOrDefault("SURREALDB_URL", "ws://localhost:8000");

	private static void withEmbedded(Consumer<Surreal> test) {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			test.accept(surreal);
		}
	}

	private static void withServer(Consumer<Surreal> test) {
		try (final Surreal surreal = new Surreal()) {
			try {
				surreal.connect(SERVER_URL);
			} catch (SurrealException e) {
				Assumptions.abort("No SurrealDB server at " + SERVER_URL);
			}
			surreal.signin(new RootCredential("root", "root"));
			surreal.useNs("test_ns").useDb("live_diff_" + System.nanoTime());
			test.accept(surreal);
		}
	}

	private static Object patch(LiveStream stream, String action) {
		final Optional<LiveNotification> n = stream.next();
		assertTrue(n.isPresent());
		assertEquals(action, n.get().getAction());
		final Array ops = n.get().getValue().getArray();
		assertEquals(1, ops.len());
		final Object op = ops.get(0).getObject();
		assertEquals("replace", op.get("op").getString());
		return op;
	}

	private static void diffNotifications(Surreal surreal) {
		surreal.query("DEFINE TABLE watched");
		if (!Surreal.atLeast(surreal.version(), 3, 3)) {
			// Diff patches can leak permission-hidden fields before 3.3.0.
			final SurrealException e = assertThrows(SurrealException.class, () -> surreal.selectLive("watched", true));
			assertTrue(e.getMessage().contains("GHSA-pm6c-q8rh-92m8"), e.getMessage());
			return;
		}
		try (final LiveStream stream = surreal.selectLive("watched", true)) {
			surreal.query("CREATE watched:1 SET v = 1, keep = 'x'");
			surreal.query("UPDATE watched:1 SET v = 2");
			surreal.query("DELETE watched:1");

			// CREATE replaces the whole (empty) document with the new record.
			final Object created = patch(stream, "CREATE");
			assertEquals("", created.get("path").getString());
			assertEquals("x", created.get("value").getObject().get("keep").getString());

			// UPDATE carries only the changed field.
			final Object updated = patch(stream, "UPDATE");
			assertEquals("/v", updated.get("path").getString());
			assertEquals(2L, updated.get("value").getLong());

			final Object deleted = patch(stream, "DELETE");
			assertEquals("", deleted.get("path").getString());
			assertTrue(deleted.get("value").isNone());
		}
	}

	private static void fullRecordsByDefault(Surreal surreal) {
		surreal.query("DEFINE TABLE watched");
		try (final LiveStream stream = surreal.selectLive("watched", false)) {
			surreal.query("CREATE watched:1 SET v = 1");
			final Optional<LiveNotification> n = stream.next();
			assertTrue(n.isPresent());
			assertEquals(1L, n.get().getValue().getObject().get("v").getLong());
		}
	}

	@Test
	void diffNotificationsEmbedded() {
		withEmbedded(LiveDiffTests::diffNotifications);
	}

	@Test
	void diffNotificationsRemote() {
		withServer(LiveDiffTests::diffNotifications);
	}

	@Test
	void fullRecordsWithoutDiffEmbedded() {
		withEmbedded(LiveDiffTests::fullRecordsByDefault);
	}

	@Test
	void fullRecordsWithoutDiffRemote() {
		withServer(LiveDiffTests::fullRecordsByDefault);
	}

	@Test
	void versionGate() {
		assertTrue(Surreal.atLeast("3.3.0", 3, 3));
		assertTrue(Surreal.atLeast("surrealdb-3.3.4", 3, 3));
		assertTrue(Surreal.atLeast("3.4.0-nightly", 3, 3));
		assertTrue(Surreal.atLeast("4.0.0", 3, 3));
		assertFalse(Surreal.atLeast("3.1.5", 3, 3));
		assertFalse(Surreal.atLeast("2.6.0", 3, 3));
		assertFalse(Surreal.atLeast("garbage", 3, 3));
	}
}
