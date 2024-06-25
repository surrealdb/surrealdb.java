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
    void createTableValue() throws SurrealException {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // Create a new Person
            final Person person = new Person("Tobie", Arrays.asList("CEO", "CTO"), 1, true, Arrays.asList(new Email("tobie@example.com", new Name("Tobie", "Foo"))));
            // Create the person in Surreal
            final Value createdValue = surreal.create("person", person);
            // We convert the value to a Person
            final Person created = createdValue.get(Person.class);
            // We now have a generated id
            person.id = created.id;
            // We check that it matches
            assertEquals(person, created);
        }
    }

    @Test
    void createTableObject() throws SurrealException {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // Create a new Person
            final Person person = new Person("Tobie", Arrays.asList("CEO", "CTO"), 1, true, Arrays.asList(new Email("tobie@example.com", new Name("Tobie", "Foo"))));
            // Create the person in Surreal
            final Person created = surreal.create(Person.class, "person", person);
            // We now have a generated id
            person.id = created.id;
            // We check that it matches
            assertEquals(person, created);
        }
    }


    @Test
    void createTableValues() {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // Create a new Person in the table `person`
            final Person tobie = new Person("Tobie", Arrays.asList("CEO", "CTO"), 1, true, Collections.singletonList(new Email("tobie@example.com", new Name("Tobie", "Foo"))));
            final Person jaime = new Person("Jaime", Collections.singletonList("COO"), 2, true, Collections.singletonList(new Email("jamie@example.com", new Name("Jamie", "Bar"))));
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

    @Test
    void createTableObjects() {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // Create a new Person in the table `person`
            final Person tobie = new Person("Tobie", Arrays.asList("CEO", "CTO"), 1, true, Collections.singletonList(new Email("tobie@example.com", new Name("Tobie", "Foo"))));
            final Person jaime = new Person("Jaime", Collections.singletonList("COO"), 2, true, Collections.singletonList(new Email("jamie@example.com", new Name("Jamie", "Bar"))));
            // We create the records
            final List<Person> created = surreal.create(Person.class, "person", tobie, jaime);
            // We check that the records are matching
            tobie.id = created.get(0).id;
            jaime.id = created.get(1).id;
            assertEquals(created, Arrays.asList(tobie, jaime));
        }
    }

    @Test
    void createThingValue() {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // Create a new Person in the table `person`
            final Person tobie = new Person("Tobie", Arrays.asList("CEO", "CTO"), 1, true, Collections.singletonList(new Email("tobie@example.com", new Name("Tobie", "Foo"))));
            // We create the records
            final Value created = surreal.create(new Thing("person", 1), tobie);
            // We check that the records are matching
            final Person person = created.get(Person.class);
            tobie.id = person.id;
            assertEquals(tobie, person);
        }
    }

    @Test
    void createThingObject() {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // Create a new Person in the table `person`
            final Person tobie = new Person("Tobie", Arrays.asList("CEO", "CTO"), 1, true, Collections.singletonList(new Email("tobie@example.com", new Name("Tobie", "Foo"))));
            // We create the records
            final Person person = surreal.create(Person.class, new Thing("person", 1), tobie);
            // We check that the records are matching
            tobie.id = person.id;
            assertEquals(tobie, person);
        }
    }

}
