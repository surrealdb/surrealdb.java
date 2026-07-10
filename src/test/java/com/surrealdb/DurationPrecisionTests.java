package com.surrealdb;

import java.time.Duration;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.surrealdb.pojos.Dates;

public class DurationPrecisionTests {

	@Test
	void readPathPreservesNanoseconds() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final Response response = surreal.query("RETURN duration::from_nanos(1234567)");
			final Value value = response.take(0);
			assertTrue(value.isDuration());
			assertEquals(Duration.ofNanos(1234567), value.getDuration());
		}
	}

	@Test
	void readPathSubMillisecondDoesNotRoundToZero() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final Response response = surreal.query("RETURN duration::from_nanos(1500)");
			final Value value = response.take(0);
			assertTrue(value.isDuration());
			assertEquals(Duration.ofNanos(1500), value.getDuration());
		}
	}

	@Test
	void readPathMixedSecondsAndNanos() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			// 1.000000500s
			final Response response = surreal.query("RETURN duration::from_nanos(1000000500)");
			final Value value = response.take(0);
			assertTrue(value.isDuration());
			assertEquals(Duration.ofSeconds(1, 500), value.getDuration());
		}
	}

	@Test
	void writePathPreservesNanoseconds() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final Response response = surreal.queryBind("RETURN $d = duration::from_nanos(1500)",
					Collections.singletonMap("d", Duration.ofNanos(1500)));
			final Value value = response.take(0);
			assertTrue(value.isBoolean());
			assertTrue(value.getBoolean(), "Bound Duration.ofNanos(1500) should equal duration::from_nanos(1500)");
		}
	}

	@Test
	void roundTripThroughBoundParameter() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final Duration[] durations = {Duration.ZERO, Duration.ofNanos(1), Duration.ofNanos(999),
					Duration.ofNanos(1500), Duration.ofNanos(1234567), Duration.ofMillis(250),
					Duration.ofSeconds(1, 500), Duration.ofSeconds(90061, 123), Duration.ofDays(365L * 5000)};
			for (final Duration d : durations) {
				final Response response = surreal.queryBind("RETURN $d", Collections.singletonMap("d", d));
				final Value value = response.take(0);
				assertTrue(value.isDuration(), "Expected a duration for " + d);
				assertEquals(d, value.getDuration(), "Round-trip mismatch for " + d);
			}
		}
	}

	@Test
	void roundTripThroughPojo() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final Dates dates = new Dates();
			dates.duration = Duration.ofSeconds(90061, 123);
			final Dates created = surreal.create(Dates.class, "duration_precision", dates).get(0);
			assertEquals(dates.duration, created.duration);
			final Dates selected = surreal.select(Dates.class, "duration_precision").next();
			assertEquals(dates.duration, selected.duration);
		}
	}

	@Test
	void negativeDurationIsRejected() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			assertThrows(SurrealException.class,
					() -> surreal.queryBind("RETURN $d", Collections.singletonMap("d", Duration.ofNanos(-1))));
			assertThrows(SurrealException.class,
					() -> surreal.queryBind("RETURN $d", Collections.singletonMap("d", Duration.ofSeconds(-5))));
		}
	}

	@Test
	void serverSideDurationBeyondJavaRangeIsRejectedOnRead() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			// 10^19 seconds exceeds Long.MAX_VALUE (~9.22 * 10^18), so this duration is
			// representable by SurrealDB (u64 seconds) but not by java.time.Duration.
			// Java cannot construct such a value; it can only arrive from the server.
			final Response response = surreal.query("RETURN 10000000000000000000s");
			final Value value = response.take(0);
			assertTrue(value.isDuration());
			final SurrealException e = assertThrows(SurrealException.class, value::getDuration);
			assertEquals("Duration of 10000000000000000000 seconds exceeds the range of java.time.Duration",
					e.getMessage());
		}
	}

	@Test
	void veryLargeDurationRoundTrips() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			// ~10,000 years with nanosecond fraction
			final Duration d = Duration.ofSeconds(315_569_520_000L, 999_999_999);
			final Response response = surreal.queryBind("RETURN $d", Collections.singletonMap("d", d));
			final Value value = response.take(0);
			assertTrue(value.isDuration());
			assertEquals(d, value.getDuration());
		}
	}
}
