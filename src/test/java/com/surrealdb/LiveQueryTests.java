package com.surrealdb;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.surrealdb.pojos.Person;

/**
 * Tests for live queries: {@link Surreal#selectLive(String)},
 * {@link LiveStream#next()}, {@link LiveStream#close()}.
 */
public class LiveQueryTests {

	@Test
	void selectLiveReturnsLiveStream() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			try (LiveStream stream = surreal.selectLive("person")) {
				assertNotNull(stream);
			}
		}
	}

	@Test
	void liveStreamNextCanBlockAndClose() throws Exception {
		AtomicReference<Optional<LiveNotification>> result = new AtomicReference<>();
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			surreal.create(Person.class, "person", Helpers.tobie);
			Thread consumer;
			try (LiveStream stream = surreal.selectLive("person")) {
				assertNotNull(stream);
				// next() blocks until a notification or close; run in background and close
				// after short timeout
				consumer = new Thread(() -> result.set(stream.next()));
				consumer.setDaemon(true);
				consumer.start();
				Thread.sleep(500);
			}
			consumer.join(2000);
			assertNotNull(result.get());
		}
	}

	@Test
	void liveStreamCloseReleases() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			try (LiveStream stream = surreal.selectLive("person")) {
				// No exception; closing again or using after close is undefined but we don't
				// crash
			}
			assertTrue(true);
		}
	}

	@Test
	void liveStreamTryWithResources() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			try (LiveStream stream = surreal.selectLive("person")) {
				assertNotNull(stream);
			}
			// stream closed automatically; no leak
		}
	}

	/**
	 * Reproduces issue #138: start a live query, block on next(), then CREATE
	 * a record — the notification must arrive. The bug report claims it never does.
	 */
	@Test
	void liveStreamReceivesCreateNotification() throws Exception {
		AtomicReference<LiveNotification> received = new AtomicReference<>();
		AtomicReference<Throwable> error = new AtomicReference<>();
		CountDownLatch consuming = new CountDownLatch(1);

		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			surreal.query("DEFINE TABLE person SCHEMALESS");

			try (LiveStream stream = surreal.selectLive("person")) {
				Thread consumer = new Thread(() -> {
					try {
						consuming.countDown();
						Optional<LiveNotification> n = stream.next();
						n.ifPresent(received::set);
					} catch (Throwable t) {
						error.set(t);
					}
				});
				consumer.setDaemon(true);
				consumer.start();

				assertTrue(consuming.await(2, TimeUnit.SECONDS),
						"Consumer thread did not start in time");
				Thread.sleep(500);

				surreal.create(new RecordId("person", 1), Helpers.tobie);

				consumer.join(5000);
				assertFalse(consumer.isAlive(),
						"Consumer thread still blocked — next() never returned");
			}

			if (error.get() != null) {
				fail("next() threw an exception: " + error.get());
			}
			assertNotNull(received.get(),
					"No notification received after CREATE");
			assertEquals("CREATE", received.get().getAction().toUpperCase());
			assertNotNull(received.get().getValue(),
					"Notification value should contain the created record");
		}
	}

	/**
	 * Reproduces issue #138 variant: live query + UPDATE should deliver
	 * a notification to next().
	 */
	@Test
	void liveStreamReceivesUpdateNotification() throws Exception {
		AtomicReference<LiveNotification> received = new AtomicReference<>();
		CountDownLatch consuming = new CountDownLatch(1);

		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			RecordId id = new RecordId("person", 1);
			surreal.create(id, Helpers.tobie);

			try (LiveStream stream = surreal.selectLive("person")) {
				Thread consumer = new Thread(() -> {
					consuming.countDown();
					Optional<LiveNotification> n = stream.next();
					n.ifPresent(received::set);
				});
				consumer.setDaemon(true);
				consumer.start();

				assertTrue(consuming.await(2, TimeUnit.SECONDS));
				Thread.sleep(200);

				surreal.update(id, UpType.MERGE, Helpers.jaime);

				consumer.join(5000);
				assertFalse(consumer.isAlive(),
						"Consumer thread still blocked — next() never returned after UPDATE");
			}

			assertNotNull(received.get(),
					"No notification received after UPDATE");
			assertEquals("UPDATE", received.get().getAction().toUpperCase());
		}
	}

	/**
	 * Tests issue #138 deadlock claim: close() from another thread must
	 * unblock a thread that is blocked on next(), without deadlocking.
	 */
	@Test
	void closeUnblocksBlockedNext() throws Exception {
		AtomicReference<Optional<LiveNotification>> result = new AtomicReference<>();
		AtomicReference<Throwable> error = new AtomicReference<>();
		CountDownLatch consuming = new CountDownLatch(1);

		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			surreal.query("DEFINE TABLE person SCHEMALESS");

			LiveStream stream = surreal.selectLive("person");
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
			assertFalse(consumer.isAlive(),
					"DEADLOCK: consumer thread still alive after close() — next() never returned");
			if (error.get() != null) {
				fail("next() threw instead of returning empty: " + error.get());
			}
			assertNotNull(result.get(), "next() should have returned after close()");
			assertFalse(result.get().isPresent(),
					"next() should return empty after stream is closed");
		}
	}

	/**
	 * Stress test for thread safety: multiple threads call next() and close()
	 * concurrently. No thread should hang or crash (SEGV / use-after-free).
	 */
	@Test
	void concurrentNextAndCloseDoesNotCrash() throws Exception {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			surreal.query("DEFINE TABLE person SCHEMALESS");
			CountDownLatch ready = new CountDownLatch(3);
			CountDownLatch go = new CountDownLatch(1);
			AtomicReference<Throwable> error = new AtomicReference<>();

			LiveStream stream = surreal.selectLive("person");

			List<Thread> threads = new ArrayList<>();
			for (int i = 0; i < 2; i++) {
				Thread t = new Thread(() -> {
					try {
						ready.countDown();
						go.await();
						stream.next();
					} catch (Throwable t1) {
						error.compareAndSet(null, t1);
					}
				});
				t.setDaemon(true);
				t.start();
				threads.add(t);
			}

			Thread closer = new Thread(() -> {
				try {
					ready.countDown();
					go.await();
					Thread.sleep(100);
					stream.close();
				} catch (Throwable t) {
					error.compareAndSet(null, t);
				}
			});
			closer.setDaemon(true);
			closer.start();
			threads.add(closer);

			assertTrue(ready.await(2, TimeUnit.SECONDS));
			go.countDown();

			for (Thread t : threads) {
				t.join(5000);
				if (t.isAlive()) {
					fail("Thread " + t.getName() + " is still alive — possible deadlock");
				}
			}

			if (error.get() != null) {
				fail("Concurrent access caused an exception: " + error.get());
			}
		}
	}

	/**
	 * Placeholder for future Surreal.kill(liveQueryId) support. The query ID is
	 * available from {@link LiveNotification#getQueryId()}, but the Java client
	 * does not yet expose kill(). Use {@link LiveStream#close()} to stop a live
	 * query.
	 */
	@Test
	@Disabled("Surreal.kill(liveQueryId) not yet in Java API")
	void killLiveQuery_byQueryId() {
		// When kill(uuid) is added: start live query, get queryId from first
		// notification or API, call surreal.kill(queryId), assert stream ends.
	}
}
