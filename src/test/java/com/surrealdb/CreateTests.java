package com.surrealdb;

import com.surrealdb.pojos.Person;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.surrealdb.Helpers.jaime;
import static com.surrealdb.Helpers.tobie;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CreateTests {

    @Test
    void createTableValue() throws SurrealException {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // Create the person in Surreal
            final Value createdValue = surreal.create("person", tobie);
            // We convert the value to a Person
            final Person created = createdValue.get(Person.class);
            // We remove the id
            created.id = null;
            // We check that it matches
            assertEquals(tobie, created);
        }
    }

    @Test
    void createTableObject() throws SurrealException {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // Create the person in Surreal
            final Person created = surreal.create(Person.class, "person", jaime);
            // We remove the id
            created.id = null;
            // We check that it matches
            assertEquals(jaime, created);
        }
    }


    @Test
    void createTableValues() {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            final List<Value> created = surreal.create("person", tobie, jaime);
            // We check that the records are matching
            final Person tobie2 = created.get(0).get(Person.class);
            tobie2.id = null;
            assertEquals(tobie, tobie2);
            final Person jaime2 = created.get(1).get(Person.class);
            jaime2.id = null;
            assertEquals(jaime, jaime2);
        }
    }

    @Test
    void createTableObjects() {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // We create the records
            final List<Person> created = surreal.create(Person.class, "person", tobie, jaime);
            // We remove the id
            created.get(0).id = null;
            created.get(1).id = null;
            // We check that the records are matching
            assertEquals(created, Arrays.asList(tobie, jaime));
        }
    }

    @Test
    void createThingValue() {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // We create the records
            final Value created = surreal.create(new RecordId("person", 1), tobie);
            // We check that the records are matching
            final Person person = created.get(Person.class);
            person.id = null;
            assertEquals(tobie, person);
        }
    }

    @Test
    void createThingObject() {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // We create the records
            final Person person = surreal.create(Person.class, new RecordId("person", 1), tobie);
            // We check that the records are matching
            person.id = null;
            assertEquals(tobie, person);
        }
    }

}
