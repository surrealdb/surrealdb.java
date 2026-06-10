package com.surrealdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class SurrealNameRecordTests {

	public record AnnotatedRecord(@SurrealName("first_name") String firstName,
			@SurrealName("login_count") int loginCount, String nickname) {
	}

	public record DuplicateRecord(@SurrealName("duplicated_name") String firstName,
			@SurrealName("duplicated_name") String secondName) {
	}

	public record BlankNameRecord(@SurrealName(" ") String firstName) {
	}

	@Test
	void serializesRecordComponentsUsingSurrealNames() {
		try (final Surreal surreal = openMemoryDatabase()) {
			final AnnotatedRecord record = new AnnotatedRecord("Ada", 7, "ace");
			final Value value = surreal.query("RETURN $record", Collections.singletonMap("record", record)).take(0);

			assertEquals(Arrays.asList("first_name", "login_count", "nickname"), keys(value.getObject()));
		}
	}

	@Test
	void deserializesRecordComponentsFromSurrealNames() {
		try (final Surreal surreal = openMemoryDatabase()) {
			final Value value = surreal.query("RETURN $record", Collections.singletonMap("record", annotatedMap()))
					.take(0);

			assertEquals(new AnnotatedRecord("Grace", 11, "g"), value.get(AnnotatedRecord.class));
		}
	}

	@Test
	void annotatedRecordComponentsDoNotAlsoAcceptRawNames() {
		try (final Surreal surreal = openMemoryDatabase()) {
			final Map<String, java.lang.Object> values = annotatedMap();
			values.put("firstName", "Raw");
			values.put("loginCount", 1);
			final Value value = surreal.query("RETURN $record", Collections.singletonMap("record", values)).take(0);

			assertEquals(new AnnotatedRecord("Grace", 11, "g"), value.get(AnnotatedRecord.class));
		}
	}

	@Test
	void rejectsDuplicateSurrealNamesForRecords() {
		try (final Surreal surreal = openMemoryDatabase()) {
			final Value value = surreal
					.query("RETURN $record",
							Collections.singletonMap("record", Collections.singletonMap("duplicated_name", "value")))
					.take(0);

			assertThrows(SurrealException.class, () -> value.get(DuplicateRecord.class));
		}
	}

	@Test
	void rejectsBlankSurrealNamesForRecords() {
		try (final Surreal surreal = openMemoryDatabase()) {
			final Value value = surreal.query("RETURN $record",
					Collections.singletonMap("record", Collections.singletonMap("firstName", "Ada"))).take(0);

			assertThrows(SurrealException.class, () -> value.get(BlankNameRecord.class));
		}
	}

	@Test
	void keepsRawRecordComponentNamesForUnannotatedComponents() {
		try (final Surreal surreal = openMemoryDatabase()) {
			final Value value = surreal
					.query("RETURN $record", Collections.singletonMap("record", new RawRecord("Ada", 7))).take(0);
			final List<String> keys = keys(value.getObject());

			assertEquals(Arrays.asList("firstName", "loginCount"), keys);
			assertFalse(keys.contains("first_name"));
			assertFalse(keys.contains("login_count"));
		}
	}

	public record RawRecord(String firstName, int loginCount) {
	}

	private static Surreal openMemoryDatabase() {
		final Surreal surreal = new Surreal();
		surreal.connect("memory").useNs("surreal_name_record_tests").useDb("surreal_name_record_tests");
		return surreal;
	}

	private static Map<String, java.lang.Object> annotatedMap() {
		final Map<String, java.lang.Object> values = new LinkedHashMap<>();
		values.put("first_name", "Grace");
		values.put("login_count", 11);
		values.put("nickname", "g");
		return values;
	}

	private static List<String> keys(final Object object) {
		final List<String> keys = new ArrayList<>();
		for (final Entry entry : object) {
			keys.add(entry.getKey());
		}
		Collections.sort(keys);
		return keys;
	}
}
