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

public class CreateTests {

    @Test
    void createTableSelectOneRecord() throws SurrealException {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // Create a new Person in the table `person`
            final Person person = new Person(null, "Tobie", Arrays.asList("CEO", "CTO"), 1, true, Arrays.asList(new Email("tobie@example.com", new Name("Tobie", "Foo"))));
            final Person created = surreal.create("person", person).get(Person.class);
            // We now have a generated id
            person.id = created.id;
            assertEquals(person, created);
            // Select the record
            final Optional<Person> selected = surreal.select(Person.class, created.id);
            assertEquals(selected, Optional.of(person));
        }
    }

    @Test
    void createTableSelectRecords() throws SurrealException {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // Create a new Person in the table `person`
            final Person tobie = new Person(null, "Tobie", Arrays.asList("CEO", "CTO"), 1, true, Arrays.asList(new Email("tobie@example.com", new Name("Tobie", "Foo"))));
            final Person jamie = new Person(null, "Jamie", Collections.singletonList("COO"), 2, true, Arrays.asList(new Email("jamie@example.com", new Name("Jamie", "Bar"))));
            final List<Value> created = surreal.create("person", tobie, jamie);
            final Person tobie2 = created.get(0).get(Person.class);
            final Person jamie2 = created.get(1).get(Person.class);
            // Select the record
            final List<Person> selected = surreal.select(Person.class, jamie2.id, tobie2.id);
            assertEquals(selected, Arrays.asList(jamie2, tobie2));
        }
    }
}
