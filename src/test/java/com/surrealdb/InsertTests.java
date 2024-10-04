package com.surrealdb;

import com.surrealdb.pojos.Person;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.surrealdb.Helpers.jaime;
import static com.surrealdb.Helpers.tobie;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class InsertTests {

    @Test
    void insertValue() throws SurrealException {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // Create the person in Surreal
            final Value value = surreal.insert("person", tobie).getFirst();
            // We convert the value to a Person
            final Person created = value.get(Person.class);
            // We remove the id
            created.id = null;
            // We check that it matches
            assertEquals(tobie, created);
        }
    }

    @Test
    void insertObject() throws SurrealException {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // Create the person in Surreal
            final Person value = surreal.insert(Person.class, "person", jaime).getFirst();
            // We remove the id
            value.id = null;
            // We check that it matches
            assertEquals(jaime, value);
        }
    }


    @Test
    void insertValues() {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            final List<Value> values = surreal.insert("person", tobie, jaime);
            // We check that the records are matching
            final Person tobie2 = values.get(0).get(Person.class);
            tobie2.id = null;
            assertEquals(tobie, tobie2);
            final Person jaime2 = values.get(1).get(Person.class);
            jaime2.id = null;
            assertEquals(jaime, jaime2);
        }
    }

    @Test
    void insertObjects() {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // We create the records
            final List<Person> values = surreal.insert(Person.class, "person", tobie, jaime);
            // We remove the id
            values.get(0).id = null;
            values.get(1).id = null;
            // We check that the records are matching
            assertEquals(values, Arrays.asList(tobie, jaime));
        }
    }


}
