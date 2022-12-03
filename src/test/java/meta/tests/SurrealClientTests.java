package meta.tests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.surrealdb.client.SurrealClient;
import com.surrealdb.client.SurrealClientSettings;
import com.surrealdb.client.SurrealTable;
import com.surrealdb.exception.SurrealRecordAlreadyExistsException;
import com.surrealdb.patch.AddPatch;
import com.surrealdb.patch.Patch;
import com.surrealdb.patch.ReplacePatch;
import com.surrealdb.query.QueryResult;
import lombok.extern.slf4j.Slf4j;
import meta.model.PartialPerson;
import meta.model.Person;
import meta.utils.TestUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
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
@Slf4j
public abstract class SurrealClientTests {

    private static final @NotNull SurrealTable<Person> personTable = SurrealTable.of("person", Person.class);

    private @UnknownNullability SurrealClient client;

    private @UnknownNullability Person tobie;
    private @UnknownNullability Person jaime;

    protected abstract @NotNull SurrealClient createClient(@NotNull SurrealClientSettings settings);

    @BeforeEach
    public void setup() {
        client = createClient(TestUtils.getClientSettings());

        client.signIn(TestUtils.getAuthCredentials());
        client.setNamespaceAndDatabase(TestUtils.getNamespace(), TestUtils.getDatabase());

        tobie = client.createRecord(personTable, "tobie", new Person("Founder & CEO", "Tobie", "Morgan Hitchcock", true));
        jaime = client.createRecord(personTable, "jaime", new Person("Founder & COO", "Jaime", "Morgan Hitchcock", true));

        log.info("Finished setup");
    }

    @AfterEach
    public void teardown() {
        log.info("Starting cleanup");

        // Delete all records created by tests
        client.deleteAllRecordsInTable(personTable);
        // Disconnect gracefully
        client.cleanup();
    }

    @Test
    void setConnectionWideParameter_whenProvidedWithAParam_setsTheParamOnTheConnection() {
        Person.Name defaultName = new Person.Name("John", "Doe");
        client.setConnectionWideParameter("default_name", defaultName);
        Person person = client.sqlSingle("CREATE person:global_param_test SET name = $default_name", Person.class).get();

        assertEquals(defaultName, person.getName());
    }

    @Test
    void sql_whenProvidedWithParams_usesThoseParamsWhenExecutingTheQuery() {
        Map<String, Object> args = ImmutableMap.of(
            "firstName", "Tobie"
        );
        List<QueryResult<Person>> actual = client.sql("SELECT * FROM person WHERE name.first = $firstName", Person.class, args);

        assertEquals(1, actual.size()); // number of queries
        assertEquals("OK", actual.get(0).getStatus()); // first query executed successfully
        assertEquals(1, actual.get(0).getResult().size()); // number of records returned
    }

    @Test
    void sqlSingle_whenTheQueryWillFindARecord_returnsANonEmptyOptionalContainingTheSpecifiedRecord() {
        Optional<Person> optionalPerson = client.sqlSingle("SELECT * FROM person ORDER BY name.first ASC LIMIT 1", Person.class);

        assertTrue(optionalPerson.isPresent());
        assertEquals(jaime, optionalPerson.get());
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

        assertEquals("Record 'person:khalid' already exists", exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void retrieveAllRecordsFromTable_whenTableExists_returnsAllRecordsFromThatTable() {
        List<Person> result = client.retrieveAllRecordsFromTable(personTable);

        assertEquals(2, result.size());

        assertEquals(result.get(0), jaime);
        assertEquals(result.get(1), tobie);
    }

    @Test
    void retrieveAllRecordsFromTable_whenTableDoesNotExist_returnsEmptyList() {
        SurrealTable<Object> table = SurrealTable.of("non_existent_table", Object.class);
        List<Object> result = client.retrieveAllRecordsFromTable(table);

        assertEquals(0, result.size());
    }

    @Test
    void retrieveRecord_whenRecordWithTheGivenIdExists_returnsSpecifiedRecord() {
        Person retrieved = client.retrieveRecord(personTable, "tobie").get();

        assertEquals(tobie, retrieved);
    }

    @Test
    void retrieveRecord_whenRecordWithTheGivenIdDoesNotExist_returnsAnEmptyOptional() {
        Optional<Person> person = client.retrieveRecord(personTable, "404");

        assertTrue(person.isEmpty());
    }

    @Test
    void setRecord_whenGivenARecordIdThatExists_updatesTheRecord() {
        Person newPerson = new Person("Engineer", "Khalid", "Alharisi", false);

        Person actual = client.setRecord(personTable, "tobie", newPerson);

        assertEquals("person:tobie", actual.getId());
        assertEquals("Engineer", actual.getTitle());
        assertEquals("Khalid", actual.getName().getFirst());
        assertEquals("Alharisi", actual.getName().getLast());
        assertFalse(actual.isMarketing());
    }

    @Test
    void setRecord_whenGivenARecordIdThatDoesNotExist_createsAndSetsRecordData() {
        Person person = new Person("Contributor", "Damian", "Kocher", false);

        Person created = assertDoesNotThrow(() -> client.setRecord(personTable, "damian", person));
        Person retrieved = client.retrieveRecord(personTable, "damian").get();

        assertEquals(created, retrieved);

        assertEquals("person:damian", retrieved.getId());
        assertEquals("Contributor", retrieved.getTitle());
        assertEquals("Damian", retrieved.getName().getFirst());
        assertEquals("Kocher", retrieved.getName().getLast());
        assertFalse(retrieved.isMarketing());
    }

    @Test
    public void setAllRecordsInTable_whenProvidedData_setsAllRecordsToThatData() {
        Person expected = new Person("Engineer", "Khalid", "Alharisi", false);

        List<Person> actual = client.setAllRecordsInTable(personTable, expected);

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

        Person changedRecord = client.changeRecord(personTable, "tobie", patch);

        // check that the record was updated
        assertFalse(changedRecord.isMarketing());

        // other values should remain unchanged
        assertEquals(tobie.getId(), changedRecord.getId());
        assertEquals(tobie.getTitle(), changedRecord.getTitle());
        assertEquals(tobie.getName(), changedRecord.getName());
    }

    @Test
    void changeAllRecordsInTable_whenProvidedPartialRecord_onlyUpdatesValuesInPartialRecord() {
        PartialPerson patch = new PartialPerson(false);

        List<Person> patchedPeople = client.changeAllRecordsInTable(personTable, patch);

        assertEquals(2, patchedPeople.size());

        Person changedJaime = patchedPeople.get(0);
        assertEquals(jaime.getId(), changedJaime.getId());
        assertEquals(jaime.getTitle(), changedJaime.getTitle());
        assertEquals(jaime.getName(), changedJaime.getName());
        assertFalse(changedJaime.isMarketing());

        Person changedTobie = patchedPeople.get(1);
        assertEquals(tobie.getId(), changedTobie.getId());
        assertEquals(tobie.getTitle(), changedTobie.getTitle());
        assertEquals(tobie.getName(), changedTobie.getName());
        assertFalse(changedTobie.isMarketing());
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
        assertEquals(jaime, deletedPeople.get(0));
        assertEquals(tobie, deletedPeople.get(1));

        List<Person> people = client.retrieveAllRecordsFromTable(personTable);
        assertEquals(0, people.size());
    }

    @Test
    void deleteAllRecordsInTable_whenProvidedWithATableContainingNoRecords_returnsEmptyList() {
        SurrealTable<Object> table = SurrealTable.of("non_existent_table", Object.class);
        List<Object> deletedRecords = client.deleteAllRecordsInTable(table);

        assertEquals(0, deletedRecords.size());
    }

    @Test
    void getNumberOfRecordsInTable_whenProvidedWithATableContainingRecords_returnsTheNumberOfRecords() {
        long count = client.getNumberOfRecordsInTable(personTable);

        assertEquals(2, count);
    }

    @Test
    void getNumberOfRecordsInTable_whenProvidedWithAnEmptyTable_returnsZeros() {
        SurrealTable<Object> emptyTable = SurrealTable.of("non_existent_table", Object.class);
        long count = client.getNumberOfRecordsInTable(emptyTable);

        assertEquals(0, count);
    }
}
