package com.surrealdb;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import org.junit.jupiter.api.Test;

import com.surrealdb.signin.RootCredential;

/**
 * Regression tests for issue
 * <a href="https://github.com/surrealdb/surrealdb.java/issues/151">#151</a>:
 * {@link Surreal#selectLive(String)} must deliver notifications over a
 * WebSocket connection, not only the in-memory engine.
 *
 * <p>
 * Notifications are exercised through every mutation entrypoint that ultimately
 * routes through {@code surreal.query(...)} in the Rust layer:
 * <ul>
 * <li>typed {@link Surreal#create(RecordId, Object)},</li>
 * <li>raw {@link Surreal#query(String)} with inline {@code CREATE},</li>
 * <li>parameterised {@link Surreal#query(String, Map)} with {@code $name}
 * bindings,</li>
 * <li>{@code CREATE type::record($id) CONTENT { ... }},</li>
 * <li>conditional {@code UPDATE ... WHERE ... RETURN BEFORE}.</li>
 * </ul>
 *
 * <p>
 * These tests require a SurrealDB server running at {@code ws://localhost:8000}
 * with a root user named {@code root}/{@code root}. CI provides one via
 * {@code surrealdb/setup-surreal} which starts SurrealDB with
 * {@code -u root -p root --unauthenticated --allow-all}. Locally, run:
 *
 * <pre>{@code
 * surreal start -u root -p root --unauthenticated --allow-all --bind 127.0.0.1:8000 memory
 * }</pre>
 *
 * If the server is unreachable, every test is skipped via {@code assumeTrue}.
 */
public class LiveQueryWebSocketTests {

	private static final String WS_URL = "ws://localhost:8000";
	private static final int CONNECT_TIMEOUT_SECONDS = 5;

	/**
	 * Opens a WS connection on a worker thread with a hard timeout so a missing
	 * server doesn't hang the suite. Returns {@code null} when unreachable, which
	 * the caller turns into a JUnit skip via
	 * {@link org.junit.jupiter.api.Assumptions#assumeTrue}.
	 */
	private static Surreal tryConnectWs() {
		Surreal surreal = new Surreal();
		ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			Future<Void> f = executor.submit(new Callable<Void>() {
				@Override
				public Void call() {
					surreal.connect(WS_URL);
					surreal.signin(new RootCredential("root", "root"));
					surreal.useNs("livetest").useDb("livetest");
					return null;
				}
			});
			f.get(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
			return surreal;
		} catch (TimeoutException | InterruptedException | java.util.concurrent.ExecutionException e) {
			surreal.close();
			return null;
		} finally {
			executor.shutdownNow();
		}
	}

	/**
	 * Blocks on {@code stream.next()} on a worker thread, runs {@code trigger}
	 * after a short delay so the subscription is definitely established, and
	 * returns whatever notification arrived (or empty if {@code next()} returned
	 * before any mutation).
	 */
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
		Thread.sleep(300);

		trigger.run();

		consumer.join(5000);
		if (err.get() != null) {
			throw new RuntimeException(err.get());
		}
		assertFalse(consumer.isAlive(), "Consumer thread still blocked — next() never returned");
		return got.get();
	}

	@Test
	void selectLive_overWebSocket_typedCreate_firesNotification() throws Exception {
		Surreal surreal = tryConnectWs();
		assumeTrue(surreal != null, "SurrealDB not reachable at " + WS_URL);
		try (Surreal s = surreal) {
			s.query("REMOVE TABLE IF EXISTS ws_person; DEFINE TABLE ws_person SCHEMALESS");
			try (LiveStream stream = s.selectLive("ws_person")) {
				Optional<LiveNotification> n = awaitOne(stream,
						() -> s.create(new RecordId("ws_person", 1), Helpers.tobie));
				assertNotNull(n.orElse(null), "No notification after typed create() over WS");
				assertEquals("CREATE", n.get().getAction().toUpperCase());
			}
		}
	}

	@Test
	void selectLive_overWebSocket_queryCreateInlineSql_firesNotification() throws Exception {
		Surreal surreal = tryConnectWs();
		assumeTrue(surreal != null, "SurrealDB not reachable at " + WS_URL);
		try (Surreal s = surreal) {
			s.query("REMOVE TABLE IF EXISTS ws_person; DEFINE TABLE ws_person SCHEMALESS");
			try (LiveStream stream = s.selectLive("ws_person")) {
				Optional<LiveNotification> n = awaitOne(stream,
						() -> s.query("CREATE ws_person:1 CONTENT { name: 'tobie' }"));
				assertNotNull(n.orElse(null), "No notification after query(\"CREATE ...\") over WS");
				assertEquals("CREATE", n.get().getAction().toUpperCase());
			}
		}
	}

	@Test
	void selectLive_overWebSocket_queryCreateWithBindings_firesNotification() throws Exception {
		Surreal surreal = tryConnectWs();
		assumeTrue(surreal != null, "SurrealDB not reachable at " + WS_URL);
		try (Surreal s = surreal) {
			s.query("REMOVE TABLE IF EXISTS ws_person; DEFINE TABLE ws_person SCHEMALESS");
			try (LiveStream stream = s.selectLive("ws_person")) {
				Map<String, java.lang.Object> params = new HashMap<>();
				params.put("name", "tobie");
				Optional<LiveNotification> n = awaitOne(stream,
						() -> s.query("CREATE ws_person:1 SET name = $name", params));
				assertNotNull(n.orElse(null), "No notification after parameterised query() CREATE over WS");
				assertEquals("CREATE", n.get().getAction().toUpperCase());
			}
		}
	}

	@Test
	void selectLive_overWebSocket_queryCreateWithTypeRecord_firesNotification() throws Exception {
		Surreal surreal = tryConnectWs();
		assumeTrue(surreal != null, "SurrealDB not reachable at " + WS_URL);
		try (Surreal s = surreal) {
			s.query("REMOVE TABLE IF EXISTS ws_person; DEFINE TABLE ws_person SCHEMALESS");
			try (LiveStream stream = s.selectLive("ws_person")) {
				Map<String, java.lang.Object> params = new HashMap<>();
				params.put("id", "ws_person:1");
				params.put("name", "tobie");
				Optional<LiveNotification> n = awaitOne(stream,
						() -> s.query("CREATE type::record($id) CONTENT { name: $name, status: 'pending' }", params));
				assertNotNull(n.orElse(null),
						"No notification after query(\"CREATE type::record($id) CONTENT ...\") over WS");
				assertEquals("CREATE", n.get().getAction().toUpperCase());
			}
		}
	}

	@Test
	void selectLive_overWebSocket_queryUpdateWhereReturnBefore_firesNotification() throws Exception {
		Surreal surreal = tryConnectWs();
		assumeTrue(surreal != null, "SurrealDB not reachable at " + WS_URL);
		try (Surreal s = surreal) {
			s.query("REMOVE TABLE IF EXISTS ws_person; DEFINE TABLE ws_person SCHEMALESS");
			s.query("CREATE ws_person:1 CONTENT { name: 'tobie', status: 'pending' }");
			try (LiveStream stream = s.selectLive("ws_person")) {
				Map<String, java.lang.Object> params = new HashMap<>();
				params.put("id", "ws_person:1");
				Optional<LiveNotification> n = awaitOne(stream, () -> s.query(
						"UPDATE type::record($id) SET status = 'completed' WHERE status = 'pending' RETURN BEFORE",
						params));
				assertNotNull(n.orElse(null), "No notification after conditional UPDATE ... RETURN BEFORE over WS");
				assertEquals("UPDATE", n.get().getAction().toUpperCase());
			}
		}
	}

	@Test
	void selectLive_overWebSocket_close_unblocksNext() throws Exception {
		Surreal surreal = tryConnectWs();
		assumeTrue(surreal != null, "SurrealDB not reachable at " + WS_URL);
		try (Surreal s = surreal) {
			s.query("REMOVE TABLE IF EXISTS ws_person; DEFINE TABLE ws_person SCHEMALESS");
			LiveStream stream = s.selectLive("ws_person");
			AtomicReference<Optional<LiveNotification>> got = new AtomicReference<>();
			CountDownLatch started = new CountDownLatch(1);
			Thread consumer = new Thread(() -> {
				started.countDown();
				got.set(stream.next());
			});
			consumer.setDaemon(true);
			consumer.start();
			started.await(2, TimeUnit.SECONDS);
			Thread.sleep(300);
			stream.close();
			consumer.join(5000);
			assertFalse(consumer.isAlive(), "next() did not return after close() over WS");
			assertNotNull(got.get(), "next() returned null Optional");
			assertFalse(got.get().isPresent(), "next() should return empty after close()");
		}
	}
}
