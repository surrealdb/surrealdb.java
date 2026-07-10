package com.surrealdb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Test;

import com.surrealdb.pojos.Person;

/**
 * Tests for filtered live queries: {@link Surreal#selectLive(String, String)}
 * and {@link Surreal#selectLive(String, String, Map)} (parity with the JS SDK's
 * {@code live(table).where(expr)}). Follows the timeout-safe patterns of
 * {@link LiveQueryTests}: {@code next()} is always called on a daemon consumer
 * thread that exits when the stream closes, so a blocking call can never hang
 * the test.
 */
public class LiveSelectWhereTests {

	/**
	 * Starts a daemon consumer thread that drains {@code stream} into
	 * {@code events} (as "ACTION id" strings) until the stream closes, and waits
	 * until it is running.
	 */
	private static Thread startConsumer(LiveStream stream, List<String> events, AtomicReference<Throwable> error)
			throws Exception {
		CountDownLatch consuming = new CountDownLatch(1);
		Thread consumer = new Thread(() -> {
			try {
				consuming.countDown();
				Optional<LiveNotification> n;
				while ((n = stream.next()).isPresent()) {
					LiveNotification notification = n.get();
					String id = notification.getValue().getObject().get("id").toString();
					events.add(notification.getAction().toUpperCase() + " " + id);
				}
			} catch (Throwable t) {
				error.set(t);
			}
		});
		consumer.setDaemon(true);
		consumer.start();
		assertTrue(consuming.await(2, TimeUnit.SECONDS), "Consumer thread did not start in time");
		Thread.sleep(500);
		return consumer;
	}

	/**
	 * Only records matching the parameterized WHERE condition produce CREATE
	 * notifications; non-matching records are filtered server-side.
	 */
	@Test
	void selectLiveWhere_onlyMatchingCreatesNotify() throws Exception {
		List<String> events = Collections.synchronizedList(new ArrayList<>());
		AtomicReference<Throwable> error = new AtomicReference<>();

		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			surreal.query("DEFINE TABLE person SCHEMALESS");

			Map<String, java.lang.Object> params = new HashMap<>();
			params.put("minCategory", 1);
			try (LiveStream stream = surreal.selectLive("person", "category > $minCategory", params)) {
				startConsumer(stream, events, error);

				// tobie has category 1 (does not match "category > 1"), jaime has
				// category 2 (matches). Create the non-matching record first: had it
				// produced a notification, it would arrive before jaime's.
				surreal.create(new RecordId("person", 1), Helpers.tobie);
				surreal.create(new RecordId("person", 2), Helpers.jaime);
				Thread.sleep(800);

				assertEquals(Collections.singletonList("CREATE person:2"), new ArrayList<>(events),
						"Only the matching CREATE should be delivered");
			}
			if (error.get() != null) {
				fail("next() threw an exception: " + error.get());
			}
		}
	}

	/**
	 * UPDATEs that move a record across the filter boundary. Observed engine
	 * semantics (embedded memory engine): the condition is evaluated against the
	 * record's NEW value, so an update moving a record INTO the filter delivers an
	 * UPDATE notification, while an update moving a record OUT of the filter
	 * delivers nothing (no DELETE-style notification).
	 */
	@Test
	void selectLiveWhere_updateAcrossFilterBoundary() throws Exception {
		List<String> events = Collections.synchronizedList(new ArrayList<>());
		AtomicReference<Throwable> error = new AtomicReference<>();

		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			// Records exist before the live query starts: no CREATE notifications.
			surreal.create(new RecordId("person", 1), Helpers.tobie); // category 1: outside filter
			surreal.create(new RecordId("person", 2), Helpers.jaime); // category 2: inside filter

			Map<String, java.lang.Object> params = new HashMap<>();
			params.put("minCategory", 1);
			try (LiveStream stream = surreal.selectLive("person", "category > $minCategory", params)) {
				startConsumer(stream, events, error);

				// Moves person:1 INTO the filter (1 -> 5): notifies with the new value.
				surreal.query("UPDATE person:1 SET category = 5");
				Thread.sleep(800);
				assertEquals(Collections.singletonList("UPDATE person:1"), new ArrayList<>(events),
						"An update moving a record into the filter should notify");

				// Moves person:2 OUT of the filter (2 -> 0): no notification.
				surreal.query("UPDATE person:2 SET category = 0");
				Thread.sleep(800);
				assertEquals(Collections.singletonList("UPDATE person:1"), new ArrayList<>(events),
						"An update moving a record out of the filter should not notify");
			}
			if (error.get() != null) {
				fail("next() threw an exception: " + error.get());
			}
		}
	}

	/**
	 * The two-argument overload (no bindings) works with a literal condition.
	 */
	@Test
	void selectLiveWhere_literalCondition() throws Exception {
		List<String> events = Collections.synchronizedList(new ArrayList<>());
		AtomicReference<Throwable> error = new AtomicReference<>();

		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			surreal.query("DEFINE TABLE person SCHEMALESS");

			try (LiveStream stream = surreal.selectLive("person", "category > 1")) {
				startConsumer(stream, events, error);

				surreal.create(new RecordId("person", 1), Helpers.tobie); // category 1: filtered out
				surreal.create(new RecordId("person", 2), Helpers.jaime); // category 2: matches
				Thread.sleep(800);

				assertEquals(Collections.singletonList("CREATE person:2"), new ArrayList<>(events));
			}
			if (error.get() != null) {
				fail("next() threw an exception: " + error.get());
			}
		}
	}

	/**
	 * The live query UUID is available up front, exactly like the unfiltered
	 * variant.
	 */
	@Test
	void selectLiveWhere_exposesQueryIdUpFront() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			surreal.query("DEFINE TABLE person SCHEMALESS");
			try (LiveStream stream = surreal.selectLive("person", "category > 1")) {
				String queryId = stream.getQueryId();
				assertNotNull(queryId, "Live query id should be available before any notification");
				assertEquals(queryId, java.util.UUID.fromString(queryId).toString());
			}
		}
	}

	/**
	 * {@link Surreal#kill(String)} stops a filtered live query: notifications
	 * before the kill arrive, none after — same contract as the unfiltered variant
	 * ({@code LiveQueryTests.killLiveQuery_byQueryId}).
	 */
	@Test
	void selectLiveWhere_killStopsNotifications() throws Exception {
		List<String> events = Collections.synchronizedList(new ArrayList<>());
		AtomicReference<Throwable> error = new AtomicReference<>();

		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			surreal.query("DEFINE TABLE person SCHEMALESS");

			try (LiveStream stream = surreal.selectLive("person", "category > 1")) {
				String queryId = stream.getQueryId();
				assertNotNull(queryId);
				startConsumer(stream, events, error);

				// A matching create before the kill is delivered.
				surreal.create(new RecordId("person", 2), Helpers.jaime);
				Thread.sleep(500);
				assertEquals(1, events.size(), "Notification before kill should be delivered");

				// After the kill, a further matching create must NOT be delivered.
				surreal.kill(queryId);
				Thread.sleep(300);
				surreal.create(new RecordId("person", 3), Helpers.emmanuel);
				Thread.sleep(800);
				assertEquals(1, events.size(), "No notifications should be delivered after kill");
			}
			if (error.get() != null) {
				fail("next() threw an exception: " + error.get());
			}
		}
	}

	/**
	 * close() unblocks a blocked next(), exactly like the unfiltered variant.
	 */
	@Test
	void selectLiveWhere_closeUnblocksBlockedNext() throws Exception {
		AtomicReference<Optional<LiveNotification>> result = new AtomicReference<>();
		AtomicReference<Throwable> error = new AtomicReference<>();
		CountDownLatch consuming = new CountDownLatch(1);

		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			surreal.query("DEFINE TABLE person SCHEMALESS");

			LiveStream stream = surreal.selectLive("person", "category > 1");
			Thread consumer = new Thread(() -> {
				try {
					consuming.countDown();
					result.set(stream.next());
				} catch (Throwable t) {
					error.set(t);
				}
			});
			consumer.setDaemon(true);
			consumer.start();

			assertTrue(consuming.await(2, TimeUnit.SECONDS));
			Thread.sleep(500);

			stream.close();

			consumer.join(5000);
			assertFalse(consumer.isAlive(), "Consumer thread still alive after close() — next() never returned");
			if (error.get() != null) {
				fail("next() threw instead of returning empty: " + error.get());
			}
			assertNotNull(result.get(), "next() should have returned after close()");
			assertFalse(result.get().isPresent(), "next() should return empty after stream is closed");
		}
	}

	/**
	 * An invalid WHERE clause surfaces an eager error, matching how selectLive
	 * surfaces subscription errors today (e.g. a non-existent table).
	 */
	@Test
	void selectLiveWhere_invalidConditionThrowsImmediately() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			surreal.query("DEFINE TABLE person SCHEMALESS");
			assertThrows(SurrealException.class, () -> surreal.selectLive("person", "][ not a condition"));
		}
	}

	/**
	 * A null or blank WHERE clause is rejected eagerly with a clear message; use
	 * {@link Surreal#selectLive(String)} for an unfiltered live query.
	 */
	@Test
	void selectLiveWhere_nullOrEmptyConditionThrows() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			surreal.query("DEFINE TABLE person SCHEMALESS");
			assertThrows(SurrealException.class, () -> surreal.selectLive("person", null));
			assertThrows(SurrealException.class, () -> surreal.selectLive("person", "  "));
		}
	}

	/**
	 * The binding name {@code tb} is reserved for the table parameter and must be
	 * rejected rather than silently overridden.
	 */
	@Test
	void selectLiveWhere_reservedParamNameThrows() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			surreal.query("DEFINE TABLE person SCHEMALESS");
			Map<String, java.lang.Object> params = new HashMap<>();
			params.put("tb", "other_table");
			assertThrows(SurrealException.class, () -> surreal.selectLive("person", "category > $tb", params));
		}
	}

	/**
	 * A filtered live query on a non-existent table throws immediately, like the
	 * unfiltered variant.
	 */
	@Test
	void selectLiveWhere_onNonExistentTableThrowsImmediately() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			assertThrows(SurrealException.class, () -> surreal.selectLive("no_such_table", "category > 1"));
		}
	}

	/**
	 * The typed Person mapping still works through a filtered stream.
	 */
	@Test
	void selectLiveWhere_notificationCarriesRecordContent() throws Exception {
		AtomicReference<LiveNotification> received = new AtomicReference<>();
		AtomicReference<Throwable> error = new AtomicReference<>();
		CountDownLatch consuming = new CountDownLatch(1);

		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			surreal.query("DEFINE TABLE person SCHEMALESS");

			Map<String, java.lang.Object> params = new HashMap<>();
			params.put("name", "Jaime");
			try (LiveStream stream = surreal.selectLive("person", "name = $name", params)) {
				Thread consumer = new Thread(() -> {
					try {
						consuming.countDown();
						stream.next().ifPresent(received::set);
					} catch (Throwable t) {
						error.set(t);
					}
				});
				consumer.setDaemon(true);
				consumer.start();

				assertTrue(consuming.await(2, TimeUnit.SECONDS));
				Thread.sleep(500);

				surreal.create(new RecordId("person", 2), Helpers.jaime);

				consumer.join(5000);
				assertFalse(consumer.isAlive(), "Consumer thread still blocked — next() never returned");
			}
			if (error.get() != null) {
				fail("next() threw an exception: " + error.get());
			}
			assertNotNull(received.get(), "No notification received after matching CREATE");
			assertEquals("CREATE", received.get().getAction().toUpperCase());
			Person person = received.get().getValue().get(Person.class);
			assertEquals(Helpers.jaime.name, person.name);
		}
	}
}
