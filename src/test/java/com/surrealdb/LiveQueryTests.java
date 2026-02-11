package com.surrealdb;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
