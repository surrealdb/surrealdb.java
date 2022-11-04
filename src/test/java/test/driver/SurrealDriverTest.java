package test.driver;

import com.google.common.collect.ImmutableMap;
import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.connection.exception.SurrealRecordAlreadyExistsException;
import com.surrealdb.driver.QueryResult;
import com.surrealdb.driver.SurrealTable;
import com.surrealdb.driver.SurrealSyncDriver;
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

    private SurrealConnection connection;
    private SurrealSyncDriver driver;

    @BeforeEach
    public void setup() {
        connection = SurrealConnection.create(TestUtils.getConnectionSettings());
        connection.connect(5);

        driver = new SurrealSyncDriver(connection);

        driver.signIn(TestUtils.getAuthCredentials());
        driver.use(TestUtils.getNamespace(), TestUtils.getDatabase());

        driver.create(personTable, "tobie", new Person("Founder & CEO", "Tobie", "Morgan Hitchcock", true));
        driver.create(personTable, "jaime", new Person("Founder & COO", "Jaime", "Morgan Hitchcock", true));
    }

    @AfterEach
    public void teardown() {
        // Delete all records created by tests
        driver.deleteRecords(personTable);
        // Disconnect gracefully
        connection.disconnect();
    }

    @Test
    void testSetConnectionWideParameter() {
        Person.Name expectedName = new Person.Name("First", "Last");
        driver.setConnectionWideParameter("default_name", expectedName);
        driver.querySingle("CREATE person:global_test SET name = $default_name", Person.class, ImmutableMap.of());

        Person person = driver.retrieveRecordFromTable(personTable, "global_test").get();

        assertEquals(expectedName, person.getName());
    }

    @Test
    public void testCreateRecordWithoutSpecifyingId() {
        Person person = new Person("Engineer", "Khalid", "Alharisi", false);
        assertNull(person.getId());

        person = driver.create(personTable, person);
        assertNotNull(person.getId());
    }

    @Test
    public void testCreateRecordWithId() {
        Person person = new Person("Engineer", "Khalid", "Alharisi", false);
        assertNull(person.getId());

        person = driver.create(personTable, "khalid", person);
        assertEquals("person:khalid", person.getId());
    }

    @Test
    public void testCreateAlreadyExistsId() {
        SurrealRecordAlreadyExistsException exception = assertThrows(SurrealRecordAlreadyExistsException.class, () -> {
            Person person = new Person("Engineer", "Khalid", "Alharisi", false);

            driver.create(personTable, "khalid", person);
            driver.create(personTable, "khalid", person);
        });

        assertEquals("person", exception.getTableName());
        assertEquals("khalid", exception.getRecordId());
    }

    @Test
    public void testQueryWithParameters() {
        Map<String, Object> params = ImmutableMap.of(
            "firstName", "Tobie"
        );
        List<QueryResult<Person>> actual = driver.query("SELECT * FROM person WHERE name.first = $firstName", Person.class, params);

        assertEquals(1, actual.size()); // number of queries
        assertEquals("OK", actual.get(0).getStatus()); // first query executed successfully
        assertEquals(1, actual.get(0).getResult().size()); // number of rows returned
    }

    @Test
    public void testQuerySingleExists() {
        Optional<Person> optionalPerson = driver.querySingle("SELECT * FROM person ORDER BY name.first DESC LIMIT 1", Person.class);

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
        expected.setId("person:tobie");

        Person actual = driver.retrieveRecordFromTable(personTable, "tobie").get();

        assertEquals(expected, actual);
    }

    @Test
    public void testSelectSingleExists() {
        Optional<Person> optionalPerson = driver.retrieveRecordFromTable(personTable, "jaime");

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
        expected.setId("person:tobie");

        Person actual = driver.updateRecord(personTable, "tobie", expected);

        assertEquals(expected, actual);
    }

    @Test
    public void testUpdateAll() {
        Person expected = new Person("Engineer", "Khalid", "Alharisi", false);

        List<Person> actual = driver.updateRecords(personTable, expected);

        assertEquals(2, actual.size());
        actual.forEach(person -> {
            assertEquals(expected.getTitle(), person.getTitle());
            assertEquals(expected.getName(), person.getName());
            assertEquals(expected.isMarketing(), person.isMarketing());
        });
    }

    @Test
    public void testChangeOne() {
        PartialPerson patch = new PartialPerson(false);

        PartialPerson actual = driver.changeRecord(personTable.withType(PartialPerson.class), "tobie", patch);

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

        driver.patchRecord(personTable, "tobie", patches);
        Person actual = driver.retrieveRecordFromTable(personTable, "tobie").get();

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
        driver.deleteRecord(personTable, "tobie");
        Optional<Person> tobie = driver.retrieveRecordFromTable(personTable, "tobie");
        assertFalse(tobie.isPresent());
    }

    @Test
    public void testDeleteAll() {
        driver.deleteRecords(personTable);
        List<Person> people = driver.retrieveAllRecordsFromTable(personTable);
        assertEquals(0, people.size());
    }
}
