package com.surrealdb;

import com.surrealdb.pojos.Person;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class LiveQueryTests {

    @Test
    void liveQueryCreateNotification() throws SurrealException {
        try (Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test_ns").useDb("test_db");

            try (LiveQueryStream<Value> stream = surreal.queryLive("LIVE SELECT * FROM person")) {
                // No notification should be pending yet.
                Optional<Notification<Value>> none = stream.pollNext(Duration.ofMillis(10));
                assertFalse(none.isPresent());

                surreal.query("CREATE person SET name = 'Ana'");

                Optional<Notification<Value>> notificationOpt = stream.pollNext(Duration.ofSeconds(2));
                assertTrue(notificationOpt.isPresent());
                Notification<Value> notification = notificationOpt.get();
                assertEquals(Action.CREATE, notification.getAction());

                Value data = notification.getDataValue();
                assertTrue(data.isObject());
                Object object = data.getObject();
                assertTrue(object.get("id").isThing());
                assertEquals("Ana", object.get("name").getString());
            }
        }
    }

    @Test
    void liveQueryNextBlockingTypedPayload() throws SurrealException {
        try (Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test_ns").useDb("test_db");

            try (LiveQueryStream<Person> stream = surreal.queryLive(Person.class, "LIVE SELECT * FROM person")) {
                surreal.query("CREATE person SET name = 'Bob'");

                Notification<Person> notification = stream.next();
                assertNotNull(notification);
                assertEquals(Action.CREATE, notification.getAction());

                Person person = notification.getData();
                assertNotNull(person);
                assertEquals("Bob", person.name);
                assertNotNull(person.id);
                assertEquals("person", person.id.getTable());
            }
        }
    }
}
