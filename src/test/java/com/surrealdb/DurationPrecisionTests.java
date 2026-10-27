package com.surrealdb;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import com.surrealdb.signin.RootCredential;

/**
 * Regression tests for durations: both directions must carry full
 * seconds-and-nanoseconds precision, and negative durations (which SurrealDB
 * cannot represent) must be rejected rather than wrapped.
 */
public class DurationPrecisionTests {

	private static final String SERVER_URL = System.getenv().getOrDefault("SURREALDB_URL", "ws://localhost:8000");

	private static final Duration SUB_MILLI = Duration.ofSeconds(90061, 123456789);

	private static void assertRoundTrips(Surreal surreal) {
		// Write path: a bound java.time.Duration keeps its nanoseconds.
		final Map<String, java.lang.Object> params = Collections.singletonMap("d", SUB_MILLI);
		final Value echoed = surreal.query("RETURN $d", params).take(0);
		assertEquals(SUB_MILLI, echoed.getDuration());
		// The server sees the exact value, not a millisecond truncation.
		final Value asString = surreal.query("RETURN <string> $d", params).take(0);
		assertEquals("1d1h1m1s123ms456µs789ns", asString.getString());
		// Read path: a duration produced by the server keeps its nanoseconds.
		final Value read = surreal.query("RETURN 1d1h1m1s123ms456µs789ns").take(0);
		assertEquals(SUB_MILLI, read.getDuration());
		final Value nanos = surreal.query("RETURN 7ns").take(0);
		assertEquals(Duration.ofNanos(7), nanos.getDuration());
	}

	@Test
	void embeddedDurationsKeepNanoseconds() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			assertRoundTrips(surreal);
		}
	}

	@Test
	void remoteDurationsKeepNanoseconds() {
		try (final Surreal surreal = new Surreal()) {
			try {
				surreal.connect(SERVER_URL);
			} catch (SurrealException e) {
				Assumptions.abort("No SurrealDB server at " + SERVER_URL);
			}
			surreal.signin(new RootCredential("root", "root"));
			surreal.useNs("test_ns").useDb("test_db");
			assertRoundTrips(surreal);
		}
	}

	@Test
	void durationBeyondLongSecondsIsRejected() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			// SurrealDB's largest durations do not fit java.time.Duration's signed
			// seconds; reading one fails instead of wrapping negative.
			final Value tooLarge = surreal.query("RETURN 9223372036854775808s").take(0);
			assertThrows(SurrealException.class, tooLarge::getDuration);
		}
	}

	@Test
	void negativeDurationIsRejected() {
		assertThrows(SurrealException.class, () -> ValueMut.createDuration(Duration.ofSeconds(-1)));
		assertThrows(SurrealException.class, () -> ValueMut.createDuration(Duration.ofNanos(-1)));
	}

	@Test
	void pojoDurationKeepsNanoseconds() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final com.surrealdb.pojos.Dates d = new com.surrealdb.pojos.Dates();
			d.dateTime = java.time.ZonedDateTime.now(java.time.ZoneId.of("UTC"));
			d.duration = SUB_MILLI;
			final com.surrealdb.pojos.Dates created = surreal.create(com.surrealdb.pojos.Dates.class, "date", d).get(0);
			assertEquals(SUB_MILLI, created.duration);
		}
	}
}
