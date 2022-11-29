package meta.tests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.surrealdb.SurrealClient;
import com.surrealdb.SurrealClientSettings;
import com.surrealdb.SurrealTable;
import com.surrealdb.exception.SurrealRecordAlreadyExistsException;
import meta.model.PartialPerson;
import meta.model.Person;
import meta.utils.TestUtils;
import com.surrealdb.patch.AddPatch;
import com.surrealdb.patch.Patch;
import com.surrealdb.patch.ReplacePatch;
import com.surrealdb.sql.QueryResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Khalid Alharisi
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
public abstract class SurrealClientTests {

    private static final SurrealTable<Person> personTable = SurrealTable.of("person", Person.class);

    private SurrealClient client;

    protected abstract SurrealClient createClient(SurrealClientSettings settings);

    @BeforeEach
    public void setup() {
        client = createClient(TestUtils.getClientSettings());
        client.connect(3, TimeUnit.SECONDS);

        client.signIn(TestUtils.getAuthCredentials());
        client.use(TestUtils.getNamespace(), TestUtils.getDatabase());

        client.createRecord(personTable, "tobie", new Person("Founder & CEO", "Tobie", "Morgan Hitchcock", true));
        client.createRecord(personTable, "jaime", new Person("Founder & COO", "Jaime", "Morgan Hitchcock", true));
    }

    @AfterEach
    public void teardown() {
        // Delete all records created by tests
        client.deleteAllRecordsInTable(personTable);
        // Disconnect gracefully
        client.disconnect();
    }

    @Test
    @Disabled("This doesn't seem to do anything")
    void testInfo() {
        client.info();
    }

    @Test
    void setConnectionWideParameter_whenProvidedWithAParam_setsTheParamOnTheConnection() {
        Person.Name expectedName = new Person.Name("First", "Last");
        client.setConnectionWideParameter("default_name", expectedName);
        Person person = client.sqlSingle("CREATE person:global_test SET name = $default_name", Person.class).get();

        assertEquals(expectedName, person.getName());
    }

    @Test
    void sql_whenProvidedWithParams_usesThoseParamsWhenExecutingTheQuery() {
        Map<String, Object> args = ImmutableMap.of(
            "firstName", "Tobie"
        );
        List<QueryResult<Person>> actual = client.sql("SELECT * FROM person WHERE name.first = $firstName", Person.class, args);

        assertEquals(1, actual.size()); // number of queries
        assertEquals("OK", actual.get(0).getStatus()); // first query executed successfully
        assertEquals(1, actual.get(0).getResult().size()); // number of rows returned
    }

    @Test
    void sqlSingle_whenTheQueryWillFindARecord_returnsANonEmptyOptionalContainingTheSpecifiedRecord() {
        Optional<Person> optionalPerson = client.sqlSingle("SELECT * FROM person ORDER BY name.first DESC LIMIT 1", Person.class);

        assertTrue(optionalPerson.isPresent());
        Person person = optionalPerson.get();
        assertEquals("Tobie", person.getName().getFirst());
    }

    @Test
    void sqlSingle_whenTheQueryWillNotFindARecord_returnsAnEmptyOptional() {
        Map<String, Object> args = ImmutableMap.of(
            "marketing", false
        );
        Optional<Person> optionalPerson = client.sqlSingle("SELECT * FROM person WHERE marketing = $marketing ORDER BY name.first DESC LIMIT 1", Person.class, args);

        assertTrue(optionalPerson.isEmpty());
    }

    @Test
    void createRecord_whenRecordNameIsNotSpecified_successfullyCreatesRecord() {
        Person person = new Person("Engineer", "Khalid", "Alharisi", false);
        assertNull(person.getId());

        Person createdPerson = client.createRecord(personTable, person);
        assertNotNull(createdPerson.getId());
    }

    @Test
    void createRecord_whenRecordNameIsSpecified_successfullyCreatesRecordWithTheGivenName() {
        Person person = new Person("Engineer", "Khalid", "Alharisi", false);
        assertNull(person.getId());

        Person createdPerson = client.createRecord(personTable, "khalid", person);
        assertEquals("person:khalid", createdPerson.getId());
    }

    @Test
    void createRecord_whenAttemptingToCreateARecordThatAlreadyExists_throwsException() {
        Person person = new Person("Engineer", "Khalid", "Alharisi", false);
        client.createRecord(personTable, "khalid", person);

        SurrealRecordAlreadyExistsException exception = assertThrows(SurrealRecordAlreadyExistsException.class, () -> {
            client.createRecord(personTable, "khalid", person);
        });

        assertEquals("person", exception.getTableName());
        assertEquals("khalid", exception.getRecordId());
    }

    @Test
    void retrieveAllRecordsFromTable_whenTableExists_returnsAllRecordsFromThatTable() {
        List<Person> result = client.retrieveAllRecordsFromTable(personTable);
        assertEquals(2, result.size());
    }

    @Test
    void retrieveAllRecordsFromTable_whenTableDoesNotExist_returnsEmptyList() {
        SurrealTable<Person> table = SurrealTable.of("non_existing_table", Person.class);
        List<Person> result = client.retrieveAllRecordsFromTable(table);
        assertEquals(0, result.size());
    }

    @Test
    void retrieveRecord_whenRecordWithTheGivenNameExists_returnsSpecifiedRecord() {
        Person expected = new Person("Founder & CEO", "Tobie", "Morgan Hitchcock", true);
        expected.setId("person:tobie");

        Person actual = client.retrieveRecord(personTable, "tobie").get();

        assertEquals(expected, actual);
    }

    @Test
    void retrieveRecord_whenRecordWithTheGivenNameDoesNotExist_returnsAnEmptyOptional() {
        Optional<Person> person = client.retrieveRecord(personTable, "404");

        assertTrue(person.isEmpty());
    }

    @Test
    public void updateRecord_whenGivenARecordThatExists_updatesTheRecord() {
        Person expected = new Person("Engineer", "Khalid", "Alharisi", false);
        expected.setId("person:tobie");

        Person actual = client.updateRecord(personTable, "tobie", expected);

        assertEquals(expected, actual);
    }

    @Test
    public void updateAllRecordsInTable_whenProvidedData_setsAllRecordsToThatData() {
        Person expected = new Person("Engineer", "Khalid", "Alharisi", false);

        List<Person> actual = client.updateAllRecordsInTable(personTable, expected);

        assertEquals(2, actual.size());
        actual.forEach(person -> {
            assertEquals(expected.getTitle(), person.getTitle());
            assertEquals(expected.getName(), person.getName());
            assertEquals(expected.isMarketing(), person.isMarketing());
        });
    }

    @Test
    void changeRecord_whenProvidedWithPartialRecord_updatesOnlyChangesValues() {
        PartialPerson patch = new PartialPerson(false);

        // TODO: Change how withType works
        PartialPerson actual = client.changeRecord(personTable.withType(PartialPerson.class), "tobie", patch);

        assertEquals(patch.isMarketing(), actual.isMarketing());
    }

    @Test
    void changeAllRecordsInTable_whenProvidedPartialRecord_onlyUpdatesValuesInPartialRecord() {
        PartialPerson patch = new PartialPerson(false);

        SurrealTable<PartialPerson> partialPersonView = personTable.withType(PartialPerson.class);
        List<PartialPerson> results = client.changeAllRecordsInTable(partialPersonView, patch);

        assertEquals(2, results.size());
        results.forEach(person -> assertEquals(patch.isMarketing(), person.isMarketing()));
    }

    @Test
    void patchRecord_whenPatchingARecordThatExists_successfullyAppliesPatches() {
        List<Patch> patches = Arrays.asList(
            ReplacePatch.create("/name/first", "Khalid"),
            ReplacePatch.create("/name/last", "Alharisi"),
            ReplacePatch.create("/title", "Engineer")
        );

        Person patchedPerson = client.patchRecord(personTable, "tobie", patches);
        Person actual = client.retrieveRecord(personTable, "tobie").get();

        assertEquals(patchedPerson, actual);

        assertEquals("Khalid", actual.getName().getFirst());
        assertEquals("Alharisi", actual.getName().getLast());
        assertEquals("Engineer", actual.getTitle());
    }

    @Test
    void patchRecord_whenPatchingARecordThatDoesNotExist_createsANewRecord() {
        List<Patch> patches = Arrays.asList(
            AddPatch.create("/name/first", "Damian"),
            AddPatch.create("/name/last", "Kocher")
        );

        Person patched = client.patchRecord(personTable, "damian", patches);
        Person actual = client.retrieveRecord(personTable, "damian").get();

        assertEquals(patched, actual);

        assertEquals("Damian", actual.getName().getFirst());
        assertEquals("Kocher", actual.getName().getLast());
        assertNull(actual.getTitle());
    }

    @Test
    void patchAllRecordsInTable_whenCalled_patchesAllRecordsInTable() {
        List<Patch> patches = ImmutableList.of(
            ReplacePatch.create("/name/first", "Khalid"),
            ReplacePatch.create("/name/last", "Alharisi"),
            ReplacePatch.create("/title", "Engineer")
        );

        List<Person> patchedPeople = client.patchAllRecordsInTable(personTable, patches);
        List<Person> actual = client.retrieveAllRecordsFromTable(personTable);

        assertEquals(patchedPeople, actual);

        assertEquals(2, actual.size());
        actual.forEach(person -> {
            assertEquals("Khalid", person.getName().getFirst());
            assertEquals("Alharisi", person.getName().getLast());
            assertEquals("Engineer", person.getTitle());
        });
    }

    @Test
    void deleteRecord_whenProvidedTheNameOfARecordThatExists_successfullyDeletesRecord() {
        Person deletedPerson = client.deleteRecord(personTable, "tobie");
        assertEquals("person:tobie", deletedPerson.getId());

        Optional<Person> tobie = client.retrieveRecord(personTable, "tobie");
        assertFalse(tobie.isPresent());
    }

    @Test
    void deleteAllRecordsInTable_whenProvidedWithATableContainingRecords_deletesAllRecords() {
        List<Person> deletedPeople = client.deleteAllRecordsInTable(personTable);
        assertEquals(2, deletedPeople.size());

        List<Person> people = client.retrieveAllRecordsFromTable(personTable);
        assertEquals(0, people.size());
    }

    @Test
    void deleteAllRecordsInTable_whenProvidedWithATableContainingNoRecords_returnsEmptyList() {
        SurrealTable<Person> table = SurrealTable.of("non_existing_table", Person.class);
        List<Person> deletedPeople = client.deleteAllRecordsInTable(table);
        assertEquals(0, deletedPeople.size());
    }
}
