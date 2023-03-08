package com.surrealdb.connection.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

/**
 * @author akaecliptic
 */
public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {

	@Override
	public void write(JsonWriter writer, LocalDateTime date) throws IOException {
		if (date == null) {
			writer.nullValue();
		} else {
			writer.value(date.toString());
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
			return (date.contains("Z") || date.contains("+")) ?
				ZonedDateTime.parse(date).toLocalDateTime() :
				LocalDateTime.parse(date);
		} catch (DateTimeParseException e) {
			throw new RuntimeException("Could not parse date '" + date + "' as LocalDateTime", e);
		}
	}

}
