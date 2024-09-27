package com.surrealdb;

import com.surrealdb.pojos.Person;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.surrealdb.Helpers.jaime;
import static com.surrealdb.Helpers.tobie;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class InsertRelationTests {


    @Test
    void insertRelationValue() {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // Create two records
            final List<Value> persons = surreal.create("person", tobie, jaime);
            // Extract the ids
            final Thing tobie = persons.get(0).get(Person.class).id;
            final Thing jaime = persons.get(1).get(Person.class).id;
            // Relate records
            final InsertRelation insert = new InsertRelation(Id.from(1), tobie, jaime);
            final Value value = surreal.insertRelation("brother", insert);
            // Get the relation:
            final InsertRelation relation = value.get(InsertRelation.class);
            // Assertion
            assertEquals(insert, relation);
        }
    }

    @Test
    void insertRelationObject() {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // Create two records
            final List<Value> persons = surreal.create("person", tobie, jaime);
            // Extract the ids
            final Thing tobie = persons.get(0).get(Person.class).id;
            final Thing jaime = persons.get(1).get(Person.class).id;
            // Relate records
            final InsertRelation insert = new InsertRelation(Id.from(1), tobie, jaime);
            final InsertRelation relation = surreal.insertRelation(InsertRelation.class, "brother", insert);
            // Assertion
            assertEquals(insert, relation);
        }
    }

    @Test
    void insertRelationValues() {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // Create two records
            final List<Value> persons = surreal.create("person", tobie, jaime);
            // Extract the ids
            final Thing tobie = persons.get(0).get(Person.class).id;
            final Thing jaime = persons.get(1).get(Person.class).id;
            // Relate records
            final InsertRelation insert1 = new InsertRelation(Id.from("A"), tobie, jaime);
            final InsertRelation insert2 = new InsertRelation(Id.from("B"), jaime, tobie);
            final List<Value> relations = surreal.insertRelations("brother", insert1, insert2);
            // Get the relation:
            final InsertRelation relation1 = relations.get(0).get(InsertRelation.class);
            final InsertRelation relation2 = relations.get(1).get(InsertRelation.class);
            // Assertion
            assertEquals(insert1, relation1);
            assertEquals(insert2, relation2);

        }
    }

    @Test
    void insertRelationObjects() {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // Create two records
            final List<Value> persons = surreal.create("person", tobie, jaime);
            // Extract the ids
            final Thing tobie = persons.get(0).get(Person.class).id;
            final Thing jaime = persons.get(1).get(Person.class).id;
            // Relate records
            final InsertRelation insert1 = new InsertRelation(Id.from(1), tobie, jaime);
            final InsertRelation insert2 = new InsertRelation(Id.from(2), jaime, tobie);
            final List<InsertRelation> relations = surreal.insertRelations(InsertRelation.class, "brother", insert1, insert2);
            // Assertion
            assertEquals(insert1, relations.get(0));
            assertEquals(insert2, relations.get(1));
        }
    }


}
