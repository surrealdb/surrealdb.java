package com.surrealdb;

import com.surrealdb.pojos.Email;
import com.surrealdb.pojos.Name;
import com.surrealdb.pojos.Person;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateTests {

    @Test
    void createTableContent() throws SurrealException {
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
            final Optional<Person> selected = surreal.select(created.id).map(v -> v.get(Person.class));
            assertEquals(selected, Optional.of(person));
        }
    }

}
