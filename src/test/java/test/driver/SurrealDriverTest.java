package test.driver;

import com.google.common.collect.ImmutableMap;
import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.connection.exception.SurrealRecordAlreadyExistsException;
import com.surrealdb.driver.SurrealTable;
import com.surrealdb.driver.SyncSurrealDriver;
import com.surrealdb.driver.QueryResult;
import com.surrealdb.driver.patch.Patch;
import com.surrealdb.driver.patch.ReplacePatch;
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
@SuppressWarnings("OptionalGetWithoutIsPresent")
public class SurrealDriverTest {

    private static final SurrealTable<Person> personTable = SurrealTable.create("person", Person.class);

    private SyncSurrealDriver driver;

    @BeforeEach
    public void setup() {
        SurrealConnection connection = SurrealConnection.create(TestUtils.getConnectionSettings());
        connection.connect(5);

        driver = new SyncSurrealDriver(connection);

        driver.signIn(TestUtils.getRootCredentials());
        driver.use(TestUtils.getNamespace(), TestUtils.getDatabase());

        driver.create(personTable, "1", new Person("Founder & CEO", "Tobie", "Morgan Hitchcock", true));
        driver.create(personTable, "2", new Person("Founder & COO", "Jaime", "Morgan Hitchcock", true));
    }

    @AfterEach
    public void teardown() {
        driver.deleteRecords(personTable);
        driver.getSurrealConnection().disconnect();
    }

    @Test
    void testSetConnectionWideParameter() {
        driver.setConnectionWideParameter("default_name", new Person.Name("First", "Last"));
        driver.querySingle("CREATE person:global_test SET name = $default_name", Person.class, ImmutableMap.of());

        Optional<Person> person = driver.retrieveRecordFromTable(personTable, "global_test");

        assertTrue(person.isPresent());
        assertEquals("First", person.get().getName().getFirst());
        assertEquals("Last", person.get().getName().getLast());
    }

    @Test
    public void testCreateNoId() {
        Person person = new Person("Engineer", "Khalid", "Alharisi", false);
        assertNull(person.getId());

        person = driver.create(personTable, person);
        assertNotNull(person.getId());
    }

    @Test
    public void testCreateWithId() {
        Person person = new Person("Engineer", "Khalid", "Alharisi", false);
        assertNull(person.getId());

        person = driver.create(personTable, "3", person);
        assertEquals("person:3", person.getId());
    }

    @Test
    public void testCreateAlreadyExistsId() {
        SurrealRecordAlreadyExistsException exception = assertThrows(SurrealRecordAlreadyExistsException.class, () -> {
            driver.create(personTable, "3", new Person("Engineer", "Khalid", "Alharisi", false));
            driver.create(personTable, "3", new Person("Engineer", "Khalid", "Alharisi", false));
        });

        assertEquals("person", exception.getTableName());
        assertEquals("3", exception.getRecordId());
    }

    @Test
    public void testQuery() {
        Map<String, Object> args = new HashMap<>();
        args.put("firstName", "Tobie");
        List<QueryResult<Person>> actual = driver.query("SELECT * FROM person WHERE name.first = $firstName", Person.class, args);

        assertEquals(1, actual.size()); // number of queries
        assertEquals("OK", actual.get(0).getStatus()); // first query executed successfully
        assertEquals(1, actual.get(0).getResult().size()); // number of rows returned
    }

    @Test
    public void testQuerySingleExists() {
        Map<String, Object> args = new HashMap<>();
        Optional<Person> optionalPerson = driver.querySingle("SELECT * FROM person ORDER BY name.first DESC LIMIT 1", Person.class, args);

        assertTrue(optionalPerson.isPresent());
        Person person = optionalPerson.get();
        assertEquals("Tobie", person.getName().getFirst());
    }

    @Test
    public void testQuerySingleWhenWhenMatchingRecordDoesNotExist() {
        Map<String, Object> args = new HashMap<>();
        args.put("marketing", "false");
        Optional<Person> optionalPerson = driver.querySingle("SELECT * FROM person WHERE marketing = $marketing ORDER BY name.first DESC LIMIT 1", Person.class, args);

        assertFalse(optionalPerson.isPresent());
    }

    @Test
    public void testSelectExists() {
        Person expected = new Person("Founder & CEO", "Tobie", "Morgan Hitchcock", true);
        expected.setId("person:1");

        Person actual = driver.retrieveRecordFromTable(personTable, "1").get();

        assertEquals(expected, actual);
    }

    @Test
    public void testSelectSingleExists() {
        Optional<Person> optionalPerson = driver.retrieveRecordFromTable(personTable, "2");

        assertTrue(optionalPerson.isPresent());
        Person person = optionalPerson.get();
        assertEquals("Jaime", person.getName().getFirst());
    }

    @Test
    public void testSelectSingleRecordDoesNotExist() {
        Optional<Person> person = driver.retrieveRecordFromTable(personTable, "404");

        assertFalse(person.isPresent());
    }

    @Test
    public void testUpdateOne() {
        Person expected = new Person("Engineer", "Khalid", "Alharisi", false);
        expected.setId("person:1");

        Person actual = driver.updateRecord(personTable, "1", expected);

        assertEquals(expected, actual);
    }

    @Test
    public void testUpdateAll() {
        Person expected = new Person("Engineer", "Khalid", "Alharisi", false);

        List<Person> actual = driver.updateRecords(personTable, expected);

        assertEquals(2, actual.size());
        actual.forEach(person -> assertEquals(expected.getTitle(), person.getTitle()));
    }

    @Test
    public void testChangeOne() {
        PartialPerson patch = new PartialPerson(false);

        PartialPerson actual = driver.changeRecord(personTable.withType(PartialPerson.class), "1", patch);

        assertEquals(patch.isMarketing(), actual.isMarketing());
    }

    @Test
    public void testChangeAll() {
        PartialPerson patch = new PartialPerson(false);

        List<PartialPerson> actual = driver.changeRecords(personTable.withType(PartialPerson.class), patch);

        assertEquals(2, actual.size());
        actual.forEach(person -> assertEquals(patch.isMarketing(), person.isMarketing()));
    }

    @Test
    public void testPatchOne() {
        List<Patch> patches = Arrays.asList(
            ReplacePatch.create("/name/first", "Khalid"),
            ReplacePatch.create("/name/last", "Alharisi"),
            ReplacePatch.create("/title", "Engineer")
        );

        driver.patchRecord(personTable, "1", patches);
        Person actual = driver.retrieveRecordFromTable(personTable, "1").get();

        assertEquals("Khalid", actual.getName().getFirst());
        assertEquals("Alharisi", actual.getName().getLast());
        assertEquals("Engineer", actual.getTitle());
    }

    @Test
    public void testPatchAll() {
        List<Patch> patches = Arrays.asList(
            ReplacePatch.create("/name/first", "Khalid"),
            ReplacePatch.create("/name/last", "Alharisi"),
            ReplacePatch.create("/title", "Engineer")
        );

        driver.patchTable(personTable, patches);
        List<Person> actual = driver.retrieveAllRecordsFromTable(personTable);

        assertEquals(2, actual.size());
        actual.forEach(person -> {
            assertEquals("Khalid", person.getName().getFirst());
            assertEquals("Alharisi", person.getName().getLast());
            assertEquals("Engineer", person.getTitle());
        });
    }

    @Test
    public void testDeleteOne() {
        driver.deleteRecord(personTable, "1");
        Optional<Person> actual = driver.retrieveRecordFromTable(personTable, "1");
        assertFalse(actual.isPresent());
    }

    @Test
    public void testDeleteAll() {
        driver.deleteRecords(personTable);
        List<Person> actual = driver.retrieveAllRecordsFromTable(personTable);
        assertEquals(0, actual.size());
    }
}
