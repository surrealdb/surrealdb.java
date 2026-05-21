package com.surrealdb;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.surrealdb.RecordHelpers.JAIME;
import static com.surrealdb.RecordHelpers.TOBIE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.surrealdb.pojos.PersonRecord;
import com.surrealdb.pojos.TypedOptionalRecord;

public class RecordRoundTripTests {

	private static <T> List<T> drain(Iterator<T> iterator) {
		final Iterable<T> iterable = () -> iterator;
		final Stream<T> stream = StreamSupport.stream(iterable.spliterator(), false);
		return stream.collect(Collectors.toList());
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
			final List<PersonRecord> normalised = people.stream().map(RecordHelpers::withoutId)
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
	void typedOptionalScalarsHydrateWithDeclaredType() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final TypedOptionalRecord input = new TypedOptionalRecord(Optional.of(42), Optional.of(3.14f),
					Optional.of((short) 7), Optional.of(99L));
			final TypedOptionalRecord readBack = surreal.create(TypedOptionalRecord.class, "tops", input).get(0);

			// instanceof checks pin the boxed runtime type — without the typed
			// Optional conversion fix, intOpt/floatOpt/shortOpt come back as
			// Long/Double/Long, and the auto-unboxing assignments below would CCE.
			assertTrue(readBack.intOpt().isPresent());
			assertTrue(readBack.intOpt().get() instanceof Integer);
			final int unboxedInt = readBack.intOpt().get();
			assertEquals(42, unboxedInt);

			assertTrue(readBack.floatOpt().isPresent());
			assertTrue(readBack.floatOpt().get() instanceof Float);
			final float unboxedFloat = readBack.floatOpt().get();
			assertEquals(3.14f, unboxedFloat, 0.001f);

			assertTrue(readBack.shortOpt().isPresent());
			assertTrue(readBack.shortOpt().get() instanceof Short);
			final short unboxedShort = readBack.shortOpt().get();
			assertEquals((short) 7, unboxedShort);

			assertTrue(readBack.longOpt().isPresent());
			assertTrue(readBack.longOpt().get() instanceof Long);
			final long unboxedLong = readBack.longOpt().get();
			assertEquals(99L, unboxedLong);
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
