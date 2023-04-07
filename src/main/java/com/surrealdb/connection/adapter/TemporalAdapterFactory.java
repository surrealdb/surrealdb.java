package com.surrealdb.connection.adapter;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.Temporal;

/**
 * An adapter to handle the parsing of {@link ZonedDateTime}, {@link LocalDateTime} and classes
 * categorised as LocalDates, ie {@link ChronoLocalDate}
 *
 * <p>While dates in the shorthand ISO-8601 (YYYY-MM-DD) format are automatically handled by
 * SurrealDB (see: <a
 * href="https://surrealdb.com/docs/surrealql/datamodel/datetimes">documentation</a>). This class
 * will explicitly convert the temporal classes listed above, before a query request is made. The
 * flavour of parsing is chosen as it closely matches SurrealDB's default format for datetime.
 *
 * <p>This will allow dates to be more consistent, even if the automatic conversion is changed
 * later. As well as, allowing for custom formatters to be used.
 *
 * @author akaecliptic
 */
public class TemporalAdapterFactory implements TypeAdapterFactory {

	private final DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
	private final ZoneId zone = ZoneOffset.UTC;

	@Override
	public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
		Class<T> rawType = (Class<T>) type.getRawType();

		if (!Temporal.class.isAssignableFrom(rawType)) {
			return null;
		} else if (ChronoLocalDate.class.isAssignableFrom(rawType)) {
			return (TypeAdapter<T>) new LocalDateAdapter();
		} else if (rawType.equals(LocalDateTime.class)) {
			return (TypeAdapter<T>) new LocalDateTimeAdapter();
		} else if (rawType.equals(ZonedDateTime.class)) {
			return (TypeAdapter<T>) new ZonedDateTimeAdapter();
		}

		return null;
	}

	private class LocalDateAdapter extends TypeAdapter<ChronoLocalDate> {

		@Override
		public void write(JsonWriter writer, ChronoLocalDate date) throws IOException {
			if (date == null) {
				writer.nullValue();
			} else {
				ZonedDateTime zdt = (ZonedDateTime) date.atTime(LocalTime.MIDNIGHT).atZone(zone);
				writer.value(formatter.format(zdt));
			}
		}

		@Override
		public ChronoLocalDate read(JsonReader reader) throws IOException {
			if (reader.peek() == JsonToken.NULL) {
				reader.nextNull();
				return null;
			}

			String date = reader.nextString();
			try {
				return ChronoLocalDate.from(formatter.parse(date));
			} catch (DateTimeParseException e) {
				throw new RuntimeException(
						"Could not parse date '" + date + "' as ChronoLocalDate", e);
			}
		}
	}

	private class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {

		@Override
		public void write(JsonWriter writer, LocalDateTime date) throws IOException {
			if (date == null) {
				writer.nullValue();
			} else {
				ZonedDateTime zdt = date.atZone(zone);
				writer.value(formatter.format(zdt));
			}
		}

		@Override
		public LocalDateTime read(JsonReader reader) throws IOException {
			if (reader.peek() == JsonToken.NULL) {
				reader.nextNull();
				return null;
			}

			String date = reader.nextString();
			try {
				return LocalDateTime.from(formatter.parse(date));
			} catch (DateTimeParseException e) {
				throw new RuntimeException(
						"Could not parse date '" + date + "' as LocalDateTime", e);
			}
		}
	}

	private class ZonedDateTimeAdapter extends TypeAdapter<ZonedDateTime> {

		@Override
		public void write(JsonWriter writer, ZonedDateTime date) throws IOException {
			if (date == null) {
				writer.nullValue();
			} else {
				writer.value(formatter.format(date));
			}
		}

		@Override
		public ZonedDateTime read(JsonReader reader) throws IOException {
			if (reader.peek() == JsonToken.NULL) {
				reader.nextNull();
				return null;
			}

			String date = reader.nextString();
			try {
				return ZonedDateTime.from(formatter.parse(date));
			} catch (DateTimeParseException e) {
				throw new RuntimeException(
						"Could not parse date '" + date + "' as ZonedDateTime", e);
			}
		}
	}
}
