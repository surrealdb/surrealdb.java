package com.surrealdb;

import com.surrealdb.pojos.Email;
import com.surrealdb.pojos.Name;
import com.surrealdb.pojos.Person;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class DeleteTests {

    @Test
    public void deleteThing() {
        try (final Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            final Person tobie = new Person("Tobie", Arrays.asList("CEO", "CTO"), 1, true, Collections.singletonList(new Email("tobie@example.com", new Name("Tobie", "Foo"))));
            final Person jaime = new Person("Jaime", Collections.singletonList("COO"), 2, true, Collections.singletonList(new Email("jamie@example.com", new Name("Jamie", "Bar"))));
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
            final Person tobie = new Person("Tobie", Arrays.asList("CEO", "CTO"), 1, true, Collections.singletonList(new Email("tobie@example.com", new Name("Tobie", "Foo"))));
            final Person jaime = new Person("Jaime", Collections.singletonList("COO"), 2, true, Collections.singletonList(new Email("jamie@example.com", new Name("Jamie", "Bar"))));
            final Person emmanuel = new Person("Emmanuel", Collections.emptyList(), 3, true, Collections.singletonList(new Email("emmanuel@example.com", new Name("Emmanuel", "Baz"))));
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
            final Person tobie = new Person("Tobie", Arrays.asList("CEO", "CTO"), 1, true, Collections.singletonList(new Email("tobie@example.com", new Name("Tobie", "Foo"))));
            final Person jaime = new Person("Jaime", Collections.singletonList("COO"), 2, true, Collections.singletonList(new Email("jamie@example.com", new Name("Jamie", "Bar"))));
            surreal.create("person", tobie, jaime);
            surreal.delete("person");
            assertFalse(surreal.select("person").hasNext());
        }
    }

}
