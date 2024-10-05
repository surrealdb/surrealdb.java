package com.surrealdb;

import com.surrealdb.pojos.Person;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.surrealdb.Helpers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class DeleteTests {

    @Test
    public void deleteThing() {
        try (final Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            final List<Person> persons = surreal.create(Person.class, "person", tobie, jaime);
            surreal.delete(persons.get(0).id);
            assertEquals(Optional.empty(), surreal.select(persons.get(0).id));
            assertEquals(Optional.of("Jaime"), surreal.select(Person.class, persons.get(1).id).map(p -> p.name));
        }
    }

    @Test
    public void deleteThings() {
        try (final Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            final List<Person> persons = surreal.create(Person.class, "person", tobie, jaime, emmanuel);
            surreal.delete(persons.get(0).id, persons.get(2).id);
            assertEquals(Optional.empty(), surreal.select(persons.get(0).id));
            assertEquals(Optional.empty(), surreal.select(persons.get(2).id));
            assertEquals(Optional.of("Jaime"), surreal.select(Person.class, persons.get(1).id).map(p -> p.name));
        }
    }

    @Test
    public void deleteTargets() {
        try (final Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            surreal.create("person", tobie, jaime);
            surreal.delete("person");
            assertFalse(surreal.select("person").hasNext());
        }
    }

}
