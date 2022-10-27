package test.driver;

import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.connection.exception.SurrealRecordAlreadyExistsException;
import com.surrealdb.driver.SyncSurrealDriver;
import com.surrealdb.driver.model.QueryResult;
import com.surrealdb.driver.model.patch.Patch;
import com.surrealdb.driver.model.patch.ReplacePatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import test.TestUtils;
import test.driver.model.PartialPerson;
import test.driver.model.Person;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Khalid Alharisi
 */
public class SurrealDriverTest {

    private SyncSurrealDriver driver;

    @BeforeEach
    public void setup() {
        SurrealConnection connection = SurrealConnection.create(TestUtils.getConnectionSettings());
        connection.connect(5);

        driver = new SyncSurrealDriver(connection);

        driver.signIn(TestUtils.getUsername(), TestUtils.getPassword());
        driver.use(TestUtils.getNamespace(), TestUtils.getDatabase());

        driver.create("person:1", new Person("Founder & CEO", "Tobie", "Morgan Hitchcock", true));
        driver.create("person:2", new Person("Founder & COO", "Jaime", "Morgan Hitchcock", true));
    }

    @AfterEach
    public void teardown() {
        driver.delete("person");
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
        assertThrows(SurrealRecordAlreadyExistsException.class, () -> {
            driver.create("person:3", new Person("Engineer", "Khalid", "Alharisi", false));
            driver.create("person:3", new Person("Engineer", "Khalid", "Alharisi", false));
        });
    }

    @Test
    public void testQuery() {
        Map<String, String> args = new HashMap<>();
        args.put("firstName", "Tobie");
        List<QueryResult<Person>> actual = driver.query("select * from person where name.first = $firstName", args, Person.class);

        assertEquals(1, actual.size()); // number of queries
        assertEquals("OK", actual.get(0).getStatus()); // first query executed successfully
        assertEquals(1, actual.get(0).getResult().size()); // number of rows returned
    }

    @Test
    public void testQuerySingleExists() {
        Map<String, String> args = new HashMap<>();
        Optional<Person> person = driver.querySingle("SELECT * FROM person ORDER BY name.first DESC LIMIT 1", args, Person.class);

        assertTrue(person.isPresent());
        assertEquals("Tobie", person.get().getName().getFirst());
    }

    @Test
    public void testQuerySingleWhenWhenMatchingRecordDoesNotExist() {
        Map<String, String> args = new HashMap<>();
        args.put("marketing", "false");
        Optional<Person> person = driver.querySingle("SELECT * FROM person WHERE marketing = $marketing ORDER BY name.first DESC LIMIT 1", args, Person.class);

        assertTrue(person.isEmpty());
    }

    @Test
    public void testSelectExists() {
        Person expected = new Person("Founder & CEO", "Tobie", "Morgan Hitchcock", true);
        expected.setId("person:1");

        List<Person> actual = driver.select("person:1", Person.class);

        assertEquals(1, actual.size());
        assertEquals(expected, actual.get(0));
    }

    @Test
    public void testSelectDoesNotExist() {
        List<Person> actual = driver.select("person:500", Person.class);
        assertEquals(0, actual.size());
    }

    @Test
    public void testSelectSingleExists() {
        Optional<Person> person = driver.selectSingle("person:2", Person.class);

        assertTrue(person.isPresent());
        assertEquals("Jaime", person.get().getName().getFirst());
    }

    @Test
    public void testSelectSingleRecordDoesNotExist() {
        Optional<Person> person = driver.selectSingle("person:404", Person.class);

        assertTrue(person.isEmpty());
    }

    @Test
    public void testUpdateOne() {
        Person expected = new Person("Engineer", "Khalid", "Alharisi", false);
        expected.setId("person:1");

        List<Person> actual = driver.update("person:1", expected);

        assertEquals(1, actual.size());
        assertEquals(expected, actual.get(0));
    }

    @Test
    public void testUpdateAll() {
        Person expected = new Person("Engineer", "Khalid", "Alharisi", false);

        List<Person> actual = driver.update("person", expected);

        assertEquals(2, actual.size());
        actual.forEach(person -> assertEquals(expected.getTitle(), person.getTitle()));
    }

    @Test
    public void testChangeOne() {
        PartialPerson patch = new PartialPerson(false);

        List<Person> actual = driver.change("person:2", patch, Person.class);

        assertEquals(1, actual.size());
        assertEquals(patch.isMarketing(), actual.get(0).isMarketing());
    }

    @Test
    public void testChangeAll() {
        PartialPerson patch = new PartialPerson(false);

        List<Person> actual = driver.change("person", patch, Person.class);

        assertEquals(2, actual.size());
        actual.forEach(person -> assertEquals(patch.isMarketing(), person.isMarketing()));
    }

    @Test
    public void testPatchOne() {
        List<Patch> patches = Arrays.asList(
            new ReplacePatch("/name/first", "Khalid"),
            new ReplacePatch("/name/last", "Alharisi"),
            new ReplacePatch("/title", "Engineer")
        );

        driver.patch("person:1", patches);
        List<Person> actual = driver.select("person:1", Person.class);

        assertEquals(1, actual.size());
        assertEquals("Khalid", actual.get(0).getName().getFirst());
        assertEquals("Alharisi", actual.get(0).getName().getLast());
        assertEquals("Engineer", actual.get(0).getTitle());
    }

    @Test
    public void testPatchAll() {
        List<Patch> patches = Arrays.asList(
            new ReplacePatch("/name/first", "Khalid"),
            new ReplacePatch("/name/last", "Alharisi"),
            new ReplacePatch("/title", "Engineer")
        );

        driver.patch("person", patches);
        List<Person> actual = driver.select("person", Person.class);

        assertEquals(2, actual.size());
        actual.forEach(person -> {
            assertEquals("Khalid", person.getName().getFirst());
            assertEquals("Alharisi", person.getName().getLast());
            assertEquals("Engineer", person.getTitle());
        });
    }

    @Test
    public void testDeleteOne() {
        driver.delete("person:1");
        List<Person> actual = driver.select("person:1", Person.class);
        assertEquals(0, actual.size());
    }

    @Test
    public void testDeleteAll() {
        driver.delete("person");
        List<Person> actual = driver.select("person", Person.class);
        assertEquals(0, actual.size());
    }
}
