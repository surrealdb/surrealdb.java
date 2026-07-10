package com.surrealdb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import static com.surrealdb.Helpers.emmanuel;
import static com.surrealdb.Helpers.jaime;
import static com.surrealdb.Helpers.tobie;
import com.surrealdb.pojos.Person;

/**
 * Tests for {@link RecordIdRange} and Surreal select/update/delete/upsert by
 * range.
 */
public class RecordIdRangeTests {

	@Test
	void selectRecordIdRangeBounded() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			surreal.create(Person.class, new RecordId("person", 1L), tobie);
			surreal.create(Person.class, new RecordId("person", 2L), jaime);
			surreal.create(Person.class, new RecordId("person", 3L), emmanuel);
			RecordIdRange range = new RecordIdRange("person", Id.from(1L), Id.from(2L));
			List<Value> selected = surreal.select(range);
			assertEquals(2, selected.size());
			List<String> names = selected.stream().map(v -> v.get(Person.class).name).sorted()
					.collect(Collectors.toList());
			assertTrue(names.contains("Tobie"));
			assertTrue(names.contains("Jaime"));
		}
	}

	@Test
	void selectRecordIdRangeUnboundedStart() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			surreal.create(Person.class, new RecordId("person", 1L), tobie);
			surreal.create(Person.class, new RecordId("person", 2L), jaime);
			surreal.create(Person.class, new RecordId("person", 3L), emmanuel);
			RecordIdRange range = new RecordIdRange("person", null, Id.from(2L));
			List<Value> selected = surreal.select(range);
			assertTrue(selected.size() >= 2);
		}
	}

	@Test
	void deleteRecordIdRange() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			surreal.create(Person.class, new RecordId("person", 1L), tobie);
			surreal.create(Person.class, new RecordId("person", 2L), jaime);
			surreal.create(Person.class, new RecordId("person", 3L), emmanuel);
			RecordIdRange range = new RecordIdRange("person", Id.from(1L), Id.from(1L));
			surreal.delete(range);
			assertEquals(Optional.empty(), surreal.select(new RecordId("person", 1L)));
			assertEquals(Optional.of("Jaime"),
					surreal.select(Person.class, new RecordId("person", 2L)).map(p -> p.name));
		}
	}

	@Test
	void updateRecordIdRange() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			surreal.create(Person.class, new RecordId("person", 1L), tobie);
			surreal.create(Person.class, new RecordId("person", 2L), jaime);
			RecordIdRange range = new RecordIdRange("person", Id.from(1L), Id.from(2L));
			surreal.update(range, UpType.MERGE,
					new Person("Updated", Collections.emptyList(), 0, false, Collections.emptyList()));
			List<Value> after = surreal.select(range);
			assertEquals(2, after.size());
			after.forEach(v -> assertEquals("Updated", v.get(Person.class).name));
		}
	}

	@Test
	void upsertRecordIdRange() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			surreal.create(Person.class, new RecordId("person", 1L), tobie);
			RecordIdRange range = new RecordIdRange("person", Id.from(1L), Id.from(2L));
			surreal.upsert(range, UpType.CONTENT, jaime);
			List<Value> after = surreal.select(range);
			assertTrue(after.size() >= 1);
		}
	}

	private static List<String> ids(List<Value> values) {
		return values.stream().map(v -> v.get(Person.class).id.toString()).sorted().collect(Collectors.toList());
	}

	private static List<String> ids(Value array) {
		assertTrue(array.isArray());
		final List<String> ids = new ArrayList<>();
		for (final Value v : array.getArray()) {
			ids.add(v.get(Person.class).id.toString());
		}
		Collections.sort(ids);
		return ids;
	}

	@Test
	void bindRecordIdRangeParamMatchesSelect() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			for (long i = 1; i <= 5; i++) {
				surreal.create(Person.class, new RecordId("person", i), tobie);
			}
			RecordIdRange range = new RecordIdRange("person", Id.from(2L), Id.from(4L));
			List<String> viaSelect = ids(surreal.select(range));
			assertEquals(3, viaSelect.size());
			Value viaQuery = surreal.query("SELECT * FROM $r", Collections.singletonMap("r", range)).take(0);
			assertEquals(viaSelect, ids(viaQuery));
			// The range and its bounds stay reusable after being bound
			assertEquals(viaSelect, ids(surreal.select(range)));
		}
	}

	@Test
	void bindRecordIdRangeParamUnboundedStart() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			for (long i = 1; i <= 5; i++) {
				surreal.create(Person.class, new RecordId("person", i), tobie);
			}
			RecordIdRange range = new RecordIdRange("person", null, Id.from(2L));
			Value viaQuery = surreal.query("SELECT * FROM $r", Collections.singletonMap("r", range)).take(0);
			assertEquals(ids(surreal.select(range)), ids(viaQuery));
			assertEquals(2, ids(viaQuery).size());
		}
	}

	@Test
	void bindRecordIdRangeParamUnboundedEnd() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			for (long i = 1; i <= 5; i++) {
				surreal.create(Person.class, new RecordId("person", i), tobie);
			}
			RecordIdRange range = new RecordIdRange("person", Id.from(4L), null);
			Value viaQuery = surreal.query("SELECT * FROM $r", Collections.singletonMap("r", range)).take(0);
			assertEquals(ids(surreal.select(range)), ids(viaQuery));
			assertEquals(2, ids(viaQuery).size());
		}
	}

	@Test
	void bindRecordIdRangeParamReturn() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			RecordIdRange range = new RecordIdRange("person", Id.from(1L), Id.from(100L));
			Value v = surreal.query("RETURN $r", Collections.singletonMap("r", range)).take(0);
			assertTrue(v.isRecordId());
			assertEquals("person:1..=100", v.toString());
		}
	}

	@Test
	void bindRecordIdRangeNestedInListParam() {
		try (Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test").useDb("test");
			RecordIdRange range = new RecordIdRange("person", Id.from(1L), Id.from(100L));
			Value v = surreal.query("RETURN $r", Collections.singletonMap("r", Collections.singletonList(range)))
					.take(0);
			assertTrue(v.isArray());
			assertEquals("person:1..=100", v.getArray().get(0).toString());
		}
	}
}
