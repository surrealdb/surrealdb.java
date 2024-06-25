package com.surrealdb;

import com.surrealdb.pojos.Email;
import com.surrealdb.pojos.Name;
import com.surrealdb.pojos.Person;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateTests {

    @Test
    void createTableRecord() throws SurrealException {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // Create a new Person
            final Person person = new Person(null, "Tobie", Arrays.asList("CEO", "CTO"), 1, true, Arrays.asList(new Email("tobie@example.com", new Name("Tobie", "Foo"))));
            // Create the person in Surreal
            final Person created = surreal.create("person", person).get(Person.class);
            // We now have a generated id
            person.id = created.id;
            // We check that it matches
            assertEquals(person, created);
        }
    }


    @Test
    void createTableSelectRecords() {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // Create a new Person in the table `person`
            final Person tobie = new Person(null, "Tobie", Arrays.asList("CEO", "CTO"), 1, true, Collections.singletonList(new Email("tobie@example.com", new Name("Tobie", "Foo"))));
            final Person jaime = new Person(null, "Jaime", Collections.singletonList("COO"), 2, true, Collections.singletonList(new Email("jamie@example.com", new Name("Jamie", "Bar"))));
            // We create the records
            final List<Value> created = surreal.create("person", tobie, jaime);
            // We check that the records are matching
            final Person tobie2 = created.get(0).get(Person.class);
            tobie.id = tobie2.id;
            assertEquals(tobie, tobie2);
            final Person jaime2 = created.get(1).get(Person.class);
            jaime.id = jaime2.id;
            assertEquals(jaime, jaime2);
        }
    }

}
