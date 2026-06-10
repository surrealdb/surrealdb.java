package com.surrealdb;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.surrealdb.pojos.JavaDates;

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

	@Test
	void convertZonedDateTimeToDateTime() {
		final ZonedDateTime dateTime = ZonedDateTime.parse("2026-06-06T10:09:10.123456789+02:00[Europe/Berlin]");

		assertBoundValueIsDateTime(dateTime, dateTime.toInstant());
	}

	@Test
	void convertPre1970InstantToDateTime() {
		final Instant instant = Instant.parse("1969-07-20T20:17:40.123456789Z");

		assertBoundValueIsDateTime(instant, instant);
	}

	@Test
	void convertSqlTimestampToDateTimeKeepingNanos() {
		final java.sql.Timestamp timestamp = java.sql.Timestamp.from(Instant.parse("2026-06-06T08:09:10.123456789Z"));

		assertBoundValueIsDateTime(timestamp, timestamp.toInstant());
	}

	@Test
	void convertSqlDateToDateTime() {
		// java.sql.Date does not support toInstant(); the epoch-millisecond
		// fallback applies.
		final java.sql.Date date = new java.sql.Date(Instant.parse("2026-06-06T08:09:10.123Z").toEpochMilli());

		assertBoundValueIsDateTime(date, Instant.ofEpochMilli(date.getTime()));
	}

	@Test
	void arrayOfSupportsDateTimeValues() {
		final Instant instant = Instant.parse("2026-06-06T08:09:10.123456789Z");
		final ZonedDateTime zonedDateTime = ZonedDateTime.parse("2026-06-06T10:09:10.123456789+02:00[Europe/Berlin]");
		final OffsetDateTime offsetDateTime = OffsetDateTime.parse("2026-06-06T10:09:10.123456789+02:00");
		final LocalDateTime localDateTime = LocalDateTime.of(2026, 6, 6, 8, 9, 10, 123456789);
		final Date date = Date.from(Instant.parse("2026-06-06T08:09:10.123Z"));

		final Array array = Array.of(instant, zonedDateTime, offsetDateTime, localDateTime, date);

		assertEquals(5, array.len());
		assertEquals(instant, array.get(0).getDateTime().toInstant());
		assertEquals(zonedDateTime.toInstant(), array.get(1).getDateTime().toInstant());
		assertEquals(offsetDateTime.toInstant(), array.get(2).getDateTime().toInstant());
		assertEquals(localDateTime.toInstant(ZoneOffset.UTC), array.get(3).getDateTime().toInstant());
		assertEquals(date.toInstant(), array.get(4).getDateTime().toInstant());
	}

	@Test
	void pojoRoundTripWithJavaDateTimeTypes() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");

			final JavaDates dates = new JavaDates();
			dates.instant = Instant.parse("2026-06-06T08:09:10.123456789Z");
			dates.offsetDateTime = OffsetDateTime.parse("2026-06-06T10:09:10.123456789+02:00");
			dates.localDateTime = LocalDateTime.of(2026, 6, 6, 8, 9, 10, 123456789);
			dates.date = Date.from(Instant.parse("2026-06-06T08:09:10.123Z"));
			dates.timestamp = java.sql.Timestamp.from(Instant.parse("2026-06-06T08:09:10.123456789Z"));
			dates.sqlDate = new java.sql.Date(Instant.parse("2026-06-06T08:09:10.123Z").toEpochMilli());
			dates.sqlTime = new java.sql.Time(Instant.parse("2026-06-06T08:09:10.123Z").toEpochMilli());

			final JavaDates created = surreal.create(JavaDates.class, "java_dates", dates).get(0);

			assertEquals(dates.instant, created.instant);
			// The offset is normalized to UTC; the point in time is preserved.
			assertEquals(dates.offsetDateTime.toInstant(), created.offsetDateTime.toInstant());
			assertEquals(dates.localDateTime, created.localDateTime);
			assertEquals(dates.date, created.date);
			assertEquals(dates.timestamp, created.timestamp);
			assertEquals(dates.sqlDate, created.sqlDate);
			assertEquals(dates.sqlTime, created.sqlTime);
		}
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
