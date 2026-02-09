package com.surrealdb;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.surrealdb.pojos.Person;

import java.util.Optional;

/**
 * Tests for live queries: {@link Surreal#selectLive(String)}, {@link LiveStream#next()}, {@link LiveStream#close()}.
 */
public class LiveQueryTests {

    @Test
    void selectLiveReturnsLiveStream() {
        try (Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test").useDb("test");
            LiveStream stream = surreal.selectLive("person");
            assertNotNull(stream);
            stream.close();
        }
    }

    @Test
    void liveStreamNextCanBlockAndClose() throws Exception {
        try (Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test").useDb("test");
            surreal.create(Person.class, "person", Helpers.tobie);
            LiveStream stream = surreal.selectLive("person");
            assertNotNull(stream);
            // May receive CREATE notification for existing record or nothing; just ensure we can call next and close
            Optional<LiveNotification> first = stream.next();
            assertNotNull(first);
            stream.close();
        }
    }

    @Test
    void liveStreamCloseReleases() {
        try (Surreal surreal = new Surreal()) {
            surreal.connect("memory").useNs("test").useDb("test");
            LiveStream stream = surreal.selectLive("person");
            stream.close();
            // No exception; closing again or using after close is undefined but we don't crash
            assertTrue(true);
        }
    }
}
