package com.surrealdb.driver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.surrealdb.BaseIntegrationTest;
import com.surrealdb.TestUtils;
import com.surrealdb.connection.SurrealWebSocketConnection;
import com.surrealdb.connection.exception.SurrealRecordAlreadyExitsException;
import com.surrealdb.connection.exception.UniqueIndexViolationException;
import com.surrealdb.driver.model.Message;
import com.surrealdb.driver.model.Movie;
import com.surrealdb.driver.model.PartialPerson;
import com.surrealdb.driver.model.Person;
import com.surrealdb.driver.model.QueryResult;
import com.surrealdb.driver.model.Reminder;
import com.surrealdb.driver.model.patch.Patch;
import com.surrealdb.driver.model.patch.ReplacePatch;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * @author Khalid Alharisi
 */
@Testcontainers
public class SurrealDriverTest extends BaseIntegrationTest {
    private final boolean connected = false;
    private SyncSurrealDriver driver;

    @BeforeEach
    public void setup() {
        final SurrealWebSocketConnection connection =
                new SurrealWebSocketConnection(testHost, testPort, false);
        connection.connect(5);
        driver = new SyncSurrealDriver(connection);

        driver.signIn(TestUtils.getUsername(), TestUtils.getPassword());
        driver.use(TestUtils.getNamespace(), TestUtils.getDatabase());

        driver.create(
                "person:1", new Person("Founder & CEO", "Tobie", "Morgan Hitchcock", true));
        driver.create(
                "person:2", new Person("Founder & COO", "Jaime", "Morgan Hitchcock", true));
    }

    @AfterEach
    public void teardown() {
        driver.delete("person");
        driver.delete("movie");
        driver.delete("message");
        driver.delete("reminder");
    }

    @Test
    public void testCreateNoId() {
        Person person = new Person("Engineer", "Khalid", "Alharisi", false);
        assertNull(person.getId());

        person = driver.create("person", person);
        assertNotNull(person.getId());
    }

    @Test
    public void testCreateWithId() {
        Person person = new Person("Engineer", "Khalid", "Alharisi", false);
        assertNull(person.getId());

        person = driver.create("person:3", person);
        assertEquals("person:3", person.getId());
    }

    @Test
    public void testCreateAlreadyExistsId() {
        assertThrows(
                SurrealRecordAlreadyExitsException.class,
                () -> {
                    driver.create(
                            "person:3", new Person("Engineer", "Khalid", "Alharisi", false));
                    driver.create(
                            "person:3", new Person("Engineer", "Khalid", "Alharisi", false));
                });
    }

    @Test
    public void testCreateAlreadyExistsUsingUniqueIndex() {
        assertThrows(
                UniqueIndexViolationException.class,
                () -> {
                    driver.query(
                            "DEFINE INDEX fullNameUniqueIndex ON TABLE person COLUMNS name.first, name.last UNIQUE",
                            Collections.emptyMap(),
                            Object.class);
                    driver.create("person", new Person("Artist", "Mia", "Mcgee", false));
                    driver.create("person", new Person("Artist", "Mia", "Mcgee", false));
                });

        // cleanup
        driver.query(
                "REMOVE INDEX fullNameUniqueIndex ON TABLE person",
                Collections.emptyMap(),
                Object.class);
    }

    @Test
    public void testQuery() {
        final Map<String, String> args = new HashMap<>();
        args.put("firstName", "Tobie");
        final List<QueryResult<Person>> actual =
                driver.query(
                        "select * from person where name.first = $firstName", args, Person.class);

        assertEquals(1, actual.size()); // number of queries
        assertEquals("OK", actual.get(0).getStatus()); // first query executed successfully
        assertEquals(1, actual.get(0).getResult().size()); // number of rows returned
    }

    @Test
    public void testSelectExists() {
        final Person expected = new Person("Founder & CEO", "Tobie", "Morgan Hitchcock", true);
        expected.setId("person:1");

        final List<Person> actual = driver.select("person:1", Person.class);

        assertEquals(1, actual.size());
        assertEquals(expected, actual.get(0));
    }

    @Test
    public void testSelectDoesNotExist() {
        final List<Person> actual = driver.select("person:500", Person.class);
        assertEquals(0, actual.size());
    }

    @Test
    public void testUpdateOne() {
        final Person expected = new Person("Engineer", "Khalid", "Alharisi", false);
        expected.setId("person:1");

        final List<Person> actual = driver.update("person:1", expected);

        assertEquals(1, actual.size());
        assertEquals(expected, actual.get(0));
    }

    @Test
    public void testUpdateAll() {
        final Person expected = new Person("Engineer", "Khalid", "Alharisi", false);

        final List<Person> actual = driver.update("person", expected);

        assertEquals(2, actual.size());
        actual.forEach(person -> assertEquals(expected.getTitle(), person.getTitle()));
    }

    @Test
    public void testChangeOne() {
        final PartialPerson patch = new PartialPerson(false);

        final List<Person> actual = driver.change("person:2", patch, Person.class);

        assertEquals(1, actual.size());
        assertEquals(patch.isMarketing(), actual.get(0).isMarketing());
    }

    @Test
    public void testChangeAll() {
        final PartialPerson patch = new PartialPerson(false);

        final List<Person> actual = driver.change("person", patch, Person.class);

        assertEquals(2, actual.size());
        actual.forEach(person -> assertEquals(patch.isMarketing(), person.isMarketing()));
    }

    @Test
    public void testPatchOne() {
        final List<Patch> patches =
                Arrays.asList(
                        new ReplacePatch("/name/first", "Khalid"),
                        new ReplacePatch("/name/last", "Alharisi"),
                        new ReplacePatch("/title", "Engineer"));

        driver.patch("person:1", patches);
        final List<Person> actual = driver.select("person:1", Person.class);

        assertEquals(1, actual.size());
        assertEquals("Khalid", actual.get(0).getName().getFirst());
        assertEquals("Alharisi", actual.get(0).getName().getLast());
        assertEquals("Engineer", actual.get(0).getTitle());
    }

    @Test
    public void testPatchAll() {
        final List<Patch> patches =
                Arrays.asList(
                        new ReplacePatch("/name/first", "Khalid"),
                        new ReplacePatch("/name/last", "Alharisi"),
                        new ReplacePatch("/title", "Engineer"));

        driver.patch("person", patches);
        final List<Person> actual = driver.select("person", Person.class);

        assertEquals(2, actual.size());
        actual.forEach(
                person -> {
                    assertEquals("Khalid", person.getName().getFirst());
                    assertEquals("Alharisi", person.getName().getLast());
                    assertEquals("Engineer", person.getTitle());
                });
    }

    @Test
    public void testDeleteOne() {
        driver.delete("person:1");
        final List<Person> actual = driver.select("person:1", Person.class);
        assertEquals(0, actual.size());
    }

    @Test
    public void testDeleteAll() {
        driver.delete("person");
        final List<Person> actual = driver.select("person", Person.class);
        assertEquals(0, actual.size());
    }

    @Test
    public void testLocalDate() {
        final LocalDate date = LocalDate.parse("2022-05-13");
        final Movie insert = new Movie("Everything Everywhere All at Once", 9, date);

        final Movie select = driver.create("movie", insert);
        assertNotNull(select.getRelease());
        assertEquals(date, select.getRelease());
    }

    @Test
    public void testLocalDateTime() {
        final LocalDateTime time = LocalDateTime.now();
        final Reminder insert = new Reminder("Pass this test", time);

        final Reminder select = driver.create("reminder", insert);
        assertNotNull(select.getTime());
        assertEquals(time, select.getTime());
    }

    @Test
    public void testZonedDateTime() {
        final ZonedDateTime time = ZonedDateTime.parse("2022-02-02T22:00:00+02:00");
        final ZonedDateTime timeAtUTC =
                LocalDateTime.parse("2022-02-02T20:00:00").atZone(ZoneOffset.UTC);
        final Message insert = new Message("This is surreal", time);

        final Message select = driver.create("message", insert);
        assertNotNull(select.getTimestamp());
        assertEquals(timeAtUTC, select.getTimestamp());
    }
}
