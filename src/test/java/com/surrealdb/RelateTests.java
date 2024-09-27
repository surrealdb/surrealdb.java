package com.surrealdb;

import com.surrealdb.pojos.Person;
import com.surrealdb.pojos.ReviewRelation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.surrealdb.Helpers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class RelateTests {


    @Test
    void relate() {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // Create two records
            final List<Value> values = surreal.create("person", tobie, jaime);
            // Extract the ids
            final Thing tobie = values.get(0).get(Person.class).id;
            final Thing jaime = values.get(1).get(Person.class).id;
            // Relate records
            final Value value = surreal.relate(tobie, "brother", jaime);
            // Get the relation:
            final Relation relation = value.get(Relation.class);
            // Assertion
            assertEquals(tobie, relation.in);
            assertEquals(jaime, relation.out);
            assertEquals("brother", relation.id.getTable());
        }
    }

    @Test
    void relateValue() {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // We create the records
            final List<Person> values = surreal.create(Person.class, "person", tobie, jaime);
            // Extract the ids
            final Thing tobie = values.get(0).id;
            final Thing jaime = values.get(1).id;
            // Relate records
            final Value value = surreal.relate(tobie, "brother", jaime, review);
            // Get a relation instance
            final ReviewRelation relation = value.get(ReviewRelation.class);
            // Assertion
            assertEquals(tobie, relation.in);
            assertEquals(jaime, relation.out);
            assertEquals(review.rate, relation.rate);
            assertEquals(review.comment, relation.comment);
            assertEquals("brother", relation.id.getTable());
        }
    }

    @Test
    void relateObject() {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // We create the records
            final List<Person> values = surreal.create(Person.class, "person", tobie, jaime);
            // Extract the ids
            final Thing tobie = values.get(0).id;
            final Thing jaime = values.get(1).id;
            // Relate records
            final ReviewRelation relation = surreal.relate(ReviewRelation.class, tobie, "brother", jaime, review);
            // Assertion
            assertEquals(tobie, relation.in);
            assertEquals(jaime, relation.out);
            assertEquals(review.rate, relation.rate);
            assertEquals(review.comment, relation.comment);
            assertEquals("brother", relation.id.getTable());
        }
    }


}
