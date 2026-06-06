package com.surrealdb;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class ValueBuilderDateTimeTests {

	@Test
	void convertInstantToDateTime() {
		final Instant instant = Instant.parse("2026-06-06T08:09:10.123456789Z");

		assertBoundValueIsDateTime(instant, instant);
	}

	@Test
	void convertOffsetDateTimeToDateTime() {
		final OffsetDateTime dateTime = OffsetDateTime.parse("2026-06-06T10:09:10.123456789+02:00");

		assertBoundValueIsDateTime(dateTime, dateTime.toInstant());
	}

	@Test
	void convertLocalDateTimeToDateTimeAtUtc() {
		final LocalDateTime dateTime = LocalDateTime.of(2026, 6, 6, 8, 9, 10, 123456789);

		assertBoundValueIsDateTime(dateTime, dateTime.toInstant(ZoneOffset.UTC));
	}

	@Test
	void convertDateToDateTime() {
		final Date date = Date.from(Instant.parse("2026-06-06T08:09:10.123Z"));

		assertBoundValueIsDateTime(date, date.toInstant());
	}

	private static void assertBoundValueIsDateTime(java.lang.Object input, Instant expected) {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");

			final Response response = surreal.query("RETURN $value", Collections.singletonMap("value", input));
			final Value value = response.take(0);

			assertTrue(value.isDateTime());
			assertEquals(expected, value.getDateTime().toInstant());
		}
	}
}
