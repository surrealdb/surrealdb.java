package com.surrealdb;

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
 * Tests for {@link RecordIdRange} and Surreal select/update/delete/upsert by range.
 */
public class RecordIdRangeTests {

    @Test
    void selectRecordIdRangeBounded() {
        try (Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test").useDb("test");
            surreal.create(Person.class, "person", tobie, jaime, emmanuel);
            RecordIdRange range = new RecordIdRange("person", Id.from(1L), Id.from(2L));
            List<Value> selected = surreal.select(range);
            assertEquals(2, selected.size());
            List<String> names = selected.stream()
                .map(v -> v.get(Person.class).name)
                .sorted()
                .collect(Collectors.toList());
            assertTrue(names.contains("Tobie"));
            assertTrue(names.contains("Jaime"));
        }
    }

    @Test
    void selectRecordIdRangeUnboundedStart() {
        try (Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test").useDb("test");
            surreal.create(Person.class, "person", tobie, jaime, emmanuel);
            RecordIdRange range = new RecordIdRange("person", null, Id.from(2L));
            List<Value> selected = surreal.select(range);
            assertTrue(selected.size() >= 2);
        }
    }

    @Test
    void deleteRecordIdRange() {
        try (Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test").useDb("test");
            List<Person> created = surreal.create(Person.class, "person", tobie, jaime, emmanuel);
            RecordIdRange range = new RecordIdRange("person", Id.from(1L), Id.from(1L));
            surreal.delete(range);
            assertEquals(Optional.empty(), surreal.select(created.get(0).id));
            assertEquals(Optional.of("Jaime"), surreal.select(Person.class, created.get(1).id).map(p -> p.name));
        }
    }

    @Test
    void updateRecordIdRange() {
        try (Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test").useDb("test");
            surreal.create(Person.class, "person", tobie, jaime);
            RecordIdRange range = new RecordIdRange("person", Id.from(1L), Id.from(2L));
            surreal.update(range, UpType.MERGE, new Person("Updated", Collections.emptyList(), 0, false, Collections.emptyList()));
            List<Value> after = surreal.select(range);
            assertEquals(2, after.size());
            after.forEach(v -> assertEquals("Updated", v.get(Person.class).name));
        }
    }

    @Test
    void upsertRecordIdRange() {
        try (Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test").useDb("test");
            surreal.create(Person.class, "person", tobie);
            RecordIdRange range = new RecordIdRange("person", Id.from(1L), Id.from(2L));
            surreal.upsert(range, UpType.CONTENT, jaime);
            List<Value> after = surreal.select(range);
            assertTrue(after.size() >= 1);
        }
    }
}
