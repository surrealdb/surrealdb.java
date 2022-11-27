package com.surrealdb.driver;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.connection.exception.SurrealRecordAlreadyExistsException;
import com.surrealdb.driver.patch.AddPatch;
import com.surrealdb.driver.patch.Patch;
import com.surrealdb.driver.patch.ReplacePatch;
import com.surrealdb.driver.sql.QueryResult;
import com.surrealdb.meta.model.PartialPerson;
import com.surrealdb.meta.model.Person;
import com.surrealdb.meta.utils.TestUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Khalid Alharisi
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
public class SurrealDriverTest {

    private static final SurrealTable<Person> personTable = SurrealTable.of("person", Person.class);

    private SurrealConnection connection;
    private SurrealDriver driver;

    @BeforeEach
    public void setup() {
        connection = SurrealConnection.create(TestUtils.getConnectionSettings());
        connection.connect(5);

        driver = SurrealDriver.create(connection);

        driver.signIn(TestUtils.getAuthCredentials());
        driver.use(TestUtils.getNamespace(), TestUtils.getDatabase());

        driver.createRecord(personTable, "tobie", new Person("Founder & CEO", "Tobie", "Morgan Hitchcock", true));
        driver.createRecord(personTable, "jaime", new Person("Founder & COO", "Jaime", "Morgan Hitchcock", true));
    }

    @AfterEach
    public void teardown() {
        // Delete all records created by tests
        driver.deleteAllRecordsInTable(personTable);
        // Disconnect gracefully
        connection.disconnect();
    }

    @Test
    void testInfo() {
        driver.info();
    }

    @Test
    void testSetConnectionWideParameter() {
        Person.Name expectedName = new Person.Name("First", "Last");
        driver.setConnectionWideParameter("default_name", expectedName);
        Person person = driver.sqlSingle("CREATE person:global_test SET name = $default_name", Person.class).get();

        assertEquals(expectedName, person.getName());
    }

    @Test
    void testQueryWithParameters() {
        Map<String, Object> args = ImmutableMap.of(
            "firstName", "Tobie"
        );
        List<QueryResult<Person>> actual = driver.sql("SELECT * FROM person WHERE name.first = $firstName", Person.class, args);

        assertEquals(1, actual.size()); // number of queries
        assertEquals("OK", actual.get(0).getStatus()); // first query executed successfully
        assertEquals(1, actual.get(0).getResult().size()); // number of rows returned
    }

    @Test
    void testQuerySingleExists() {
        Optional<Person> optionalPerson = driver.sqlSingle("SELECT * FROM person ORDER BY name.first DESC LIMIT 1", Person.class);

        assertTrue(optionalPerson.isPresent());
        Person person = optionalPerson.get();
        assertEquals("Tobie", person.getName().getFirst());
    }

    @Test
    void testQuerySingleWhenWhenMatchingRecordDoesNotExist() {
        Map<String, Object> args = ImmutableMap.of(
            "marketing", false
        );
        Optional<Person> optionalPerson = driver.sqlSingle("SELECT * FROM person WHERE marketing = $marketing ORDER BY name.first DESC LIMIT 1", Person.class, args);

        assertTrue(optionalPerson.isEmpty());
    }

    @Test
    void createRecord_whenRecordNameIsNotSpecified_successfullyCreatesRecord() {
        Person person = new Person("Engineer", "Khalid", "Alharisi", false);
        assertNull(person.getId());

        Person createdPerson = driver.createRecord(personTable, person);
        assertNotNull(createdPerson.getId());
    }

    @Test
    void createRecord_whenRecordNameIsSpecified_successfullyCreatesRecordWithTheGivenName() {
        Person person = new Person("Engineer", "Khalid", "Alharisi", false);
        assertNull(person.getId());

        Person createdPerson = driver.createRecord(personTable, "khalid", person);
        assertEquals("person:khalid", createdPerson.getId());
    }

    @Test
    void createRecord_whenAttemptingToCreateARecordThatAlreadyExists_throwsException() {
        Person person = new Person("Engineer", "Khalid", "Alharisi", false);
        driver.createRecord(personTable, "khalid", person);

        SurrealRecordAlreadyExistsException exception = assertThrows(SurrealRecordAlreadyExistsException.class, () -> {
            driver.createRecord(personTable, "khalid", person);
        });

        assertEquals("person", exception.getTableName());
        assertEquals("khalid", exception.getRecordId());
    }

    @Test
    void retrieveAllRecordsFromTable_whenTableExists_returnsAllRecordsFromThatTable() {
        List<Person> result = driver.retrieveAllRecordsFromTable(personTable);
        assertEquals(2, result.size());
    }

    @Test
    void retrieveAllRecordsFromTable_whenTableDoesNotExist_returnsEmptyList() {
        SurrealTable<Person> table = SurrealTable.of("non_existing_table", Person.class);
        List<Person> result = driver.retrieveAllRecordsFromTable(table);
        assertEquals(0, result.size());
    }

    @Test
    void retrieveRecord_whenRecordWithTheGivenNameExists_returnsSpecifiedRecord() {
        Person expected = new Person("Founder & CEO", "Tobie", "Morgan Hitchcock", true);
        expected.setId("person:tobie");

        Person actual = driver.retrieveRecord(personTable, "tobie").get();

        assertEquals(expected, actual);
    }

    @Test
    void retrieveRecord_whenRecordWithTheGivenNameDoesNotExist_returnsAnEmptyOptional() {
        Optional<Person> person = driver.retrieveRecord(personTable, "404");

        assertTrue(person.isEmpty());
    }

    @Test
    public void updateRecord_whenGivenARecordThatExists_updatesTheRecord() {
        Person expected = new Person("Engineer", "Khalid", "Alharisi", false);
        expected.setId("person:tobie");

        Person actual = driver.updateRecord(personTable, "tobie", expected);

        assertEquals(expected, actual);
    }

    @Test
    public void updateAllRecordsInTable_() {
        Person expected = new Person("Engineer", "Khalid", "Alharisi", false);

        List<Person> actual = driver.updateAllRecordsInTable(personTable, expected);

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
        PartialPerson actual = driver.changeRecord(personTable.withType(PartialPerson.class), "tobie", patch);

        assertEquals(patch.isMarketing(), actual.isMarketing());
    }

    @Test
    void changeAllRecordsInTable_whenProvidedPartialRecord_onlyUpdatesValuesInPartialRecord() {
        PartialPerson patch = new PartialPerson(false);

        SurrealTable<PartialPerson> partialPersonView = personTable.withType(PartialPerson.class);
        List<PartialPerson> results = driver.changeAllRecordsInTable(partialPersonView, patch);

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

        Person patchedPerson = driver.patchRecord(personTable, "tobie", patches);
        Person actual = driver.retrieveRecord(personTable, "tobie").get();

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

        Person patched = driver.patchRecord(personTable, "damian", patches);
        Person actual = driver.retrieveRecord(personTable, "damian").get();

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

        List<Person> patchedPeople = driver.patchAllRecordsInTable(personTable, patches);
        List<Person> actual = driver.retrieveAllRecordsFromTable(personTable);

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
        Person deletedPerson = driver.deleteRecord(personTable, "tobie");
        assertEquals("person:tobie", deletedPerson.getId());

        Optional<Person> tobie = driver.retrieveRecord(personTable, "tobie");
        assertFalse(tobie.isPresent());
    }

    @Test
    void deleteAllRecordsInTable_whenProvidedWithATableContainingRecords_deletesAllRecords() {
        List<Person> deletedPeople = driver.deleteAllRecordsInTable(personTable);
        assertEquals(2, deletedPeople.size());

        List<Person> people = driver.retrieveAllRecordsFromTable(personTable);
        assertEquals(0, people.size());
    }

    @Test
    void deleteAllRecordsInTable_whenProvidedWithATableContainingNoRecords_returnsEmptyList() {
        SurrealTable<Person> table = SurrealTable.of("non_existing_table", Person.class);
        List<Person> deletedPeople = driver.deleteAllRecordsInTable(table);
        assertEquals(0, deletedPeople.size());
    }
}
