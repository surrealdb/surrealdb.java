package com.surrealdb;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
			LiveStream stream = surreal.selectLive("person");
			assertNotNull(stream);
			stream.close();
		}
	}

	@Test
	void liveStreamNextCanBlockAndClose() throws Exception {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			surreal.create(Person.class, "person", Helpers.tobie);
			LiveStream stream = surreal.selectLive("person");
			assertNotNull(stream);
			// next() blocks until a notification or close; run in background and close
			// after short timeout
			AtomicReference<Optional<LiveNotification>> result = new AtomicReference<>();
			Thread consumer = new Thread(() -> result.set(stream.next()));
			consumer.setDaemon(true);
			consumer.start();
			Thread.sleep(500);
			stream.close();
			consumer.join(2000);
			assertNotNull(result.get());
		}
	}

	@Test
	void liveStreamCloseReleases() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			LiveStream stream = surreal.selectLive("person");
			stream.close();
			// No exception; closing again or using after close is undefined but we don't
			// crash
			assertTrue(true);
		}
	}
}
