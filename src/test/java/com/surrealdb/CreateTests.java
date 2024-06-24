package com.surrealdb;

import com.surrealdb.pojos.Email;
import com.surrealdb.pojos.Name;
import com.surrealdb.pojos.Person;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateTests {

    @Test
    @Disabled
    void createTableContent() throws SurrealException {
        try (final Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            final Person person = new Person(null, "Tobie", Arrays.asList("CEO", "CTO"), 1, true, Arrays.asList(new Email("tobie@example.com", new Name("Tobie", "Foo"))));
            final Person created = surreal.create("person", person).get(Person.class);
            person.id = created.id;
            assertEquals(person, created);
            final Iterator<Person> result = surreal.select(created.id, Person.class);
            assertEquals(result.next(), person);
        }
    }

}
