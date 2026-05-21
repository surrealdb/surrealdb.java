package com.surrealdb;

import java.util.Iterator;
import java.util.List;

import static com.surrealdb.RecordHelpers.JAIME;
import static com.surrealdb.RecordHelpers.TOBIE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.surrealdb.pojos.PersonRecord;

public class RecordCrudTests {

	@Test
	void insertRecordObject() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final PersonRecord inserted = surreal.insert(PersonRecord.class, "person", JAIME).get(0);
			assertNotNull(inserted.id());
			assertEquals(JAIME.name(), inserted.name());
			assertEquals(JAIME.tags(), inserted.tags());
			assertEquals(JAIME.category(), inserted.category());
			assertEquals(JAIME.emails(), inserted.emails());
		}
	}

	@Test
	void insertRecordObjects() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final List<PersonRecord> inserted = surreal.insert(PersonRecord.class, "person", TOBIE, JAIME);
			assertEquals(2, inserted.size());
			assertEquals(TOBIE.name(), inserted.get(0).name());
			assertEquals(JAIME.name(), inserted.get(1).name());
		}
	}

	@Test
	void updateRecordIdObject() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final RecordId id = new RecordId("person", 1);
			surreal.create(PersonRecord.class, id, TOBIE);
			final PersonRecord updated = surreal.update(PersonRecord.class, id, UpType.CONTENT, JAIME);
			assertEquals(JAIME.name(), updated.name());
			assertEquals(JAIME.category(), updated.category());
			assertEquals(JAIME.nickname(), updated.nickname());
		}
	}

	@Test
	void updateRecordTableObjects() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			surreal.create(PersonRecord.class, new RecordId("person", 1), TOBIE);
			surreal.create(PersonRecord.class, new RecordId("person", 2), TOBIE);
			final Iterator<PersonRecord> updated = surreal.update(PersonRecord.class, "person", UpType.CONTENT, JAIME);
			int count = 0;
			while (updated.hasNext()) {
				assertEquals(JAIME.name(), updated.next().name());
				count++;
			}
			assertEquals(2, count);
		}
	}

	@Test
	void upsertRecordIdObject() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			final RecordId id = new RecordId("person", 1);
			// No pre-existing row: upsert acts as create.
			final PersonRecord upserted = surreal.upsert(PersonRecord.class, id, UpType.CONTENT, JAIME);
			assertEquals(JAIME.name(), upserted.name());
			assertEquals(JAIME.nickname(), upserted.nickname());
		}
	}

	@Test
	void upsertRecordTableObjects() {
		try (final Surreal surreal = new Surreal()) {
			surreal.connect("memory").useNs("test_ns").useDb("test_db");
			surreal.create(PersonRecord.class, new RecordId("person", 1), TOBIE);
			surreal.create(PersonRecord.class, new RecordId("person", 2), TOBIE);
			final Iterator<PersonRecord> upserted = surreal.upsert(PersonRecord.class, "person", UpType.CONTENT, JAIME);
			// Mirrors the POJO suite: assert each returned row matches without pinning
			// the row count, since upsert-on-table semantics differ from update-on-table.
			boolean any = false;
			while (upserted.hasNext()) {
				assertEquals(JAIME.name(), upserted.next().name());
				any = true;
			}
			assertTrue(any);
		}
	}
}
