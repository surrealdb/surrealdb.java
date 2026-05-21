package com.surrealdb;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.surrealdb.pojos.EmailRecord;
import com.surrealdb.pojos.NameRecord;
import com.surrealdb.pojos.PersonRecord;

public class RecordRoundTripTests {

	private static final PersonRecord TOBIE = new PersonRecord(null, "Tobie", Arrays.asList("CEO", "CTO"), 1L, true,
			Collections.singletonList(new EmailRecord("tobie@example.com", new NameRecord("Tobie", "Foo"))),
			Optional.of("toby"));

	private static final PersonRecord JAIME = new PersonRecord(null, "Jaime", Collections.singletonList("COO"), 2L,
			true, Collections.singletonList(new EmailRecord("jaime@example.com", new NameRecord("Jaime", "Bar"))),
			Optional.empty());

	private static <T> List<T> drain(Iterator<T> iterator) {
		final Iterable<T> iterable = () -> iterator;
		final Stream<T> stream = StreamSupport.stream(iterable.spliterator(), false);
		return stream.collect(Collectors.toList());
	}

	// Records have final fields so we can't null out `id` like the POJO tests do.
	// Compare records by erasing the id from the read-back instance.
	private static PersonRecord withoutId(PersonRecord p) {
		return new PersonRecord(null, p.name(), p.tags(), p.category(), p.active(), p.emails(), p.nickname());
	}

	@Test
	void createAndSelectViaRecordId() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final PersonRecord created = surreal.create(PersonRecord.class, "person", TOBIE).get(0);
			assertNotNull(created.id());
			final Optional<PersonRecord> readBack = surreal.select(PersonRecord.class, created.id());
			assertTrue(readBack.isPresent());
			assertEquals(created.name(), readBack.get().name());
			assertEquals(TOBIE.tags(), readBack.get().tags());
			assertEquals(TOBIE.category(), readBack.get().category());
			assertEquals(TOBIE.active(), readBack.get().active());
			assertEquals(TOBIE.emails(), readBack.get().emails());
			assertEquals(TOBIE.nickname(), readBack.get().nickname());
		}
	}

	@Test
	void selectListedRecordIds() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final List<PersonRecord> created = surreal.create(PersonRecord.class, "person", TOBIE, JAIME);
			final List<PersonRecord> selected = surreal.select(PersonRecord.class, created.get(0).id(),
					created.get(1).id());
			assertEquals(2, selected.size());
			assertEquals(TOBIE.name(), selected.get(0).name());
			assertEquals(JAIME.name(), selected.get(1).name());
		}
	}

	@Test
	void selectTableViaIterator() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			surreal.create(PersonRecord.class, "person", TOBIE, JAIME);
			final Iterator<PersonRecord> iterator = surreal.select(PersonRecord.class, "person");
			final List<PersonRecord> people = drain(iterator);
			assertEquals(2, people.size());
			final List<PersonRecord> normalised = people.stream().map(RecordRoundTripTests::withoutId)
					.collect(Collectors.toList());
			assertTrue(normalised.contains(TOBIE));
			assertTrue(normalised.contains(JAIME));
		}
	}

	@Test
	void optionalComponentPresentAndAbsent() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final PersonRecord created = surreal.create(PersonRecord.class, "person", TOBIE).get(0);
			assertEquals(Optional.of("toby"), created.nickname());

			final PersonRecord createdJaime = surreal.create(PersonRecord.class, "person", JAIME).get(0);
			assertEquals(Optional.empty(), createdJaime.nickname());
		}
	}

	@Test
	void minimalRecordMatchesUserReproducer() {
		// The reproducer from the user: a small record with two String components.
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("my_ns").useDb("my_db");
			surreal.create("record", new MyRecord("foo", "myTenant"));
			final Iterator<MyRecord> iterator = surreal.select(MyRecord.class, "record");
			final List<MyRecord> rows = drain(iterator);
			assertEquals(1, rows.size());
			assertEquals("foo", rows.get(0).name());
			assertEquals("myTenant", rows.get(0).tenant());
		}
	}

	public record MyRecord(String name, String tenant) {
	}
}
