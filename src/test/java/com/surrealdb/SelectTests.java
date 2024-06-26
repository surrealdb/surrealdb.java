package com.surrealdb;

import com.surrealdb.pojos.Person;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SelectTests implements AutoCloseable {

    final Surreal surreal;
    final Person tobie;
    final Person jaime;

    public SelectTests() {
        // Starts an embedded in memory instance
        this.surreal = new Surreal();
        surreal.connect("memory").useNs("test_ns").useDb("test_db");
        // Create a new Person(s) in the table `person`
        final List<Value> created = surreal.create("person", Helpers.tobie, Helpers.jaime);
        this.tobie = created.get(0).get(Person.class);
        this.jaime = created.get(1).get(Person.class);
    }

    public static <T> List<T> toList(Iterator<T> iterator, boolean parallel) {
        final Iterable<T> iterable = () -> iterator;
        final Stream<T> stream = StreamSupport.stream(iterable.spliterator(), parallel);
        return stream.collect(Collectors.toList());
    }

    @Test
    public void selectOneExistingObject() {
        final Optional<Person> person = surreal.select(Person.class, jaime.id);
        assertEquals(Optional.of(jaime), person);
    }

    @Test
    public void selectNonExistingObject() {
        final Optional<Person> person = surreal.select(Person.class, new Thing("Dummy", 1));
        assertEquals(Optional.empty(), person);
    }

    @Test
    public void selectListedObjects() {
        final List<Person> selected = surreal.select(Person.class, jaime.id, tobie.id);
        assertEquals(selected, Arrays.asList(jaime, tobie));
    }

    @Test
    public void selectTableValuesWithIterator() {
        final Iterator<Value> iterator = surreal.select("person");
        // Get the values
        final List<Value> values = toList(iterator, false);
        // Convert them to Person(s)
        final List<Person> persons = values.stream().map(v -> v.get(Person.class)).collect(Collectors.toList());
        // Check the result
        assertTrue(persons.contains(tobie));
        assertTrue(persons.contains(jaime));
    }

    @Test
    public void selectTableValuesWithSynchronizedIterator() {
        final Iterator<Value> iterator = surreal.selectSync("person");
        // Get the values
        final List<Value> values = toList(iterator, true);
        // Convert them to Person(s)
        final List<Person> persons = values.stream().map(v -> v.get(Person.class)).collect(Collectors.toList());
        // Check the result
        assertTrue(persons.contains(tobie));
        assertTrue(persons.contains(jaime));
    }

    @Test
    public void selectTableObjectsWithIterator() {
        final Iterator<Person> iterator = surreal.select(Person.class, "person");
        // Get the list
        final List<Person> persons = toList(iterator, false);
        // Check the result
        assertTrue(persons.contains(tobie));
        assertTrue(persons.contains(jaime));
    }

    @Test
    public void selectTableObjectsWithSynchronizedIterator() {
        final Iterator<Person> iterator = surreal.selectSync(Person.class, "person");
        // Get the list
        final List<Person> persons = toList(iterator, true);
        // Check the result
        assertTrue(persons.contains(tobie));
        assertTrue(persons.contains(jaime));
    }

    @Override
    public void close() {
        surreal.close();
    }
}
