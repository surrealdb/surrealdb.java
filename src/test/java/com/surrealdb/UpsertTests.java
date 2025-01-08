package com.surrealdb;

import com.surrealdb.pojos.Person;
import org.junit.jupiter.api.Test;

import java.util.Iterator;

import static com.surrealdb.Helpers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class UpsertTests {

    @Test
    void upsertThingValue() throws SurrealException {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // Build an id
            final RecordId id = new RecordId("person", 1);
            // Upsert the person in SurrealDB
            final Value updated = surreal.upsert(id, UpType.CONTENT, jaime);
            // Check the person has been updated
            assertEquals("Jaime", updated.get(Person.class).name);
        }
    }

    @Test
    void upsertThingObject() throws SurrealException {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // Build an id
            final RecordId id = new RecordId("person", 1);
            // Update the person in SurrealDB
            final Person updated = surreal.upsert(Person.class, id, UpType.CONTENT, jaime);
            // Check the person has been updated
            assertEquals("Jaime", updated.name);
        }
    }

    @Test
    void upsertTableValues() {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // We create some records
            surreal.create(new RecordId("person", 1), tobie);
            surreal.create(new RecordId("person", 2), emmanuel);
            // We update the records
            final Iterator<Value> updated = surreal.upsert("person", UpType.CONTENT, jaime);
            // Check the updated values
            updated.forEachRemaining(value -> {
                assertEquals("Jaime", value.getObject().get("name").getString());
            });
        }
    }

    @Test
    void upsertTableValuesSync() {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // We create some records
            surreal.create(new RecordId("person", 1), tobie);
            surreal.create(new RecordId("person", 2), emmanuel);
            // We update the records
            final Iterator<Value> updated = surreal.upsertSync("person", UpType.CONTENT, jaime);
            // Check the updated values
            updated.forEachRemaining(value -> {
                assertEquals("Jaime", value.getObject().get("name").getString());
            });
        }
    }

    @Test
    void upsertTableObjects() {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // We create some records
            surreal.create(new RecordId("person", 1), tobie);
            surreal.create(new RecordId("person", 2), emmanuel);
            // We update the records
            final Iterator<Person> updated = surreal.upsert(Person.class, "person", UpType.CONTENT, jaime);
            // Check the updated values
            updated.forEachRemaining(person -> {
                assertEquals("Jaime", person.name);
            });
        }
    }

    @Test
    void upsertTableObjectsSync() {
        try (final Surreal surreal = new Surreal()) {
            // Starts an embedded in memory instance
            surreal.connect("memory").useNs("test_ns").useDb("test_db");
            // We create some records
            surreal.create(new RecordId("person", 1), tobie);
            surreal.create(new RecordId("person", 2), emmanuel);
            // We update the records
            final Iterator<Person> updated = surreal.upsertSync(Person.class, "person", UpType.CONTENT, jaime);
            // Check the updated values
            updated.forEachRemaining(person -> {
                assertEquals("Jaime", person.name);
            });
        }
    }

}
