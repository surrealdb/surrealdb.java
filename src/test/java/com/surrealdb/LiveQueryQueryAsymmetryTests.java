package com.surrealdb;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

/**
 * Coverage gap pinned down by the investigation of issue
 * <a href="https://github.com/surrealdb/surrealdb.java/issues/151">#151</a>:
 * {@link Surreal.LiveStream} notifications must fire for every mutation
 * entrypoint, not only the typed {@link Surreal#create(RecordId, Object)}
 * overload that the existing {@link LiveQueryTests} suite exercises.
 *
 * <p>
 * In practice every {@code create()} / {@code update()} overload and every
 * {@link Surreal#query(String)} / {@link Surreal#query(String, java.util.Map)}
 * variant routes through {@code surrealdb_query} → {@code surreal.query(...)}
 * in {@code src/main/rust/surreal.rs}, so an SDK-layer asymmetry isn't
 * architecturally possible. These tests lock that in for the in-memory engine.
 * The WebSocket counterpart lives in {@link LiveQueryWebSocketTests}.
 */
public class LiveQueryQueryAsymmetryTests {

	private static Optional<LiveNotification> awaitOne(LiveStream stream, Runnable trigger) throws Exception {
		AtomicReference<Optional<LiveNotification>> got = new AtomicReference<>(Optional.empty());
		AtomicReference<Throwable> err = new AtomicReference<>();
		CountDownLatch started = new CountDownLatch(1);

		Thread consumer = new Thread(() -> {
			try {
				started.countDown();
				got.set(stream.next());
			} catch (Throwable t) {
				err.set(t);
			}
		});
		consumer.setDaemon(true);
		consumer.start();

		started.await(2, TimeUnit.SECONDS);
		Thread.sleep(200);

		trigger.run();

		consumer.join(5000);
		if (err.get() != null) {
			throw new RuntimeException(err.get());
		}
		assertFalse(consumer.isAlive(), "Consumer thread still blocked");
		return got.get();
	}

	@Test
	void query_createWithRawSql_firesNotification() throws Exception {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			surreal.query("DEFINE TABLE person SCHEMALESS");

			try (LiveStream stream = surreal.selectLive("person")) {
				Optional<LiveNotification> n = awaitOne(stream,
						() -> surreal.query("CREATE person:1 CONTENT { name: 'tobie' }"));
				assertNotNull(n.orElse(null), "No notification after query(\"CREATE ...\")");
				assertEquals("CREATE", n.get().getAction().toUpperCase());
			}
		}
	}

	@Test
	void query_createWithBindings_firesNotification() throws Exception {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			surreal.query("DEFINE TABLE person SCHEMALESS");

			try (LiveStream stream = surreal.selectLive("person")) {
				Map<String, java.lang.Object> params = new HashMap<>();
				params.put("name", "tobie");
				Optional<LiveNotification> n = awaitOne(stream,
						() -> surreal.query("CREATE person:1 SET name = $name", params));
				assertNotNull(n.orElse(null), "No notification after query(...) with bindings");
				assertEquals("CREATE", n.get().getAction().toUpperCase());
			}
		}
	}

	@Test
	void query_createWithTypeRecord_firesNotification() throws Exception {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			surreal.query("DEFINE TABLE person SCHEMALESS");

			try (LiveStream stream = surreal.selectLive("person")) {
				Map<String, java.lang.Object> params = new HashMap<>();
				params.put("id", "person:1");
				params.put("name", "tobie");
				Optional<LiveNotification> n = awaitOne(stream, () -> surreal
						.query("CREATE type::record($id) CONTENT { name: $name, status: 'pending' }", params));
				assertNotNull(n.orElse(null), "No notification after query(\"CREATE type::record($id) CONTENT ...\")");
				assertEquals("CREATE", n.get().getAction().toUpperCase());
			}
		}
	}

	@Test
	void query_updateWithWhereReturnBefore_firesNotification() throws Exception {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			surreal.query("DEFINE TABLE person SCHEMALESS");
			surreal.query("CREATE person:1 CONTENT { name: 'tobie', status: 'pending' }");

			try (LiveStream stream = surreal.selectLive("person")) {
				Map<String, java.lang.Object> params = new HashMap<>();
				params.put("id", "person:1");
				Optional<LiveNotification> n = awaitOne(stream, () -> surreal.query(
						"UPDATE type::record($id) SET status = 'completed' WHERE status = 'pending' RETURN BEFORE",
						params));
				assertNotNull(n.orElse(null), "No notification after conditional UPDATE ... RETURN BEFORE");
				assertEquals("UPDATE", n.get().getAction().toUpperCase());
			}
		}
	}

	@Test
	void query_updateSimple_firesNotification() throws Exception {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			surreal.query("DEFINE TABLE person SCHEMALESS");
			surreal.query("CREATE person:1 CONTENT { name: 'tobie' }");

			try (LiveStream stream = surreal.selectLive("person")) {
				Optional<LiveNotification> n = awaitOne(stream,
						() -> surreal.query("UPDATE person:1 SET name = 'jaime'"));
				assertNotNull(n.orElse(null), "No notification after plain UPDATE via query()");
				assertEquals("UPDATE", n.get().getAction().toUpperCase());
			}
		}
	}
}
