package com.surrealdb.connection.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

/**
 * @author akaecliptic
 */
public class LocalDateAdapter extends TypeAdapter<LocalDate> {

	@Override
	public void write(JsonWriter writer, LocalDate date) throws IOException {
		if (date == null) {
			writer.nullValue();
		} else {
			writer.value(date.toString());
		}
	}

	@Override
	public LocalDate read(JsonReader reader) throws IOException {
		if (reader.peek() == JsonToken.NULL) {
			reader.nextNull();
			return null;
		}

		String date = reader.nextString();

		try {
			return (date.contains("Z") || date.contains("+")) ?
				ZonedDateTime.parse(date).toLocalDate() :
				LocalDate.parse(date);
		} catch (DateTimeParseException e) {
			throw new RuntimeException("Could not parse date '" + date + "' as LocalDate", e);
		}
	}

}
