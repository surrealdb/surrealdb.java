package test.driver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.driver.SurrealDriver;
import com.surrealdb.driver.SurrealTable;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import test.TestUtils;
import test.driver.model.InstantContainer;
import test.driver.model.Person;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * E2E serialization tests
 */
public class SurrealDriverGsonTest {

    private static final SurrealTable<Person> personTable = SurrealTable.of("person", Person.class);
    private static final SurrealTable<InstantContainer> timeTable = SurrealTable.of("time", InstantContainer.class);

    private SurrealDriver driver;

    @AfterEach
    void cleanup() {
        if (driver != null) {
            driver.deleteAllRecordsInTable(personTable);
            driver.deleteAllRecordsInTable(timeTable);
        }
    }

    void setupDriver(Gson gson) {
        val connectionSettings = TestUtils.createConnectionSettingsBuilderWithDefaults()
            .setGson(gson)
            .setAutoConnect(true)
            .build();

        val connection = SurrealConnection.create(connectionSettings);

        driver = SurrealDriver.create(connection);
        driver.signIn(TestUtils.getAuthCredentials());
        driver.use(TestUtils.getNamespace(), TestUtils.getDatabase());
    }

    @Test
    void testGsonWithPrettyPrintingDoesNotBreakSerialization() {
        val gson = new GsonBuilder().setPrettyPrinting().create();
        setupDriver(gson);

        val person = new Person("Contributor", "Damian", "Kocher", false);
        assertDoesNotThrow(() -> driver.createRecord(personTable, "damian", person));

        val optionalPersonFromDb = driver.retrieveRecord(personTable, "damian");
        assertTrue(optionalPersonFromDb.isPresent());
        Person personFromDb = optionalPersonFromDb.get();

        assertEquals(person.getTitle(), personFromDb.getTitle());
        assertEquals(person.getName(), personFromDb.getName());
        assertEquals(person.isMarketing(), personFromDb.isMarketing());
    }

    @Test
    void testGsonWithHtmlEscapingDoesNotBreakSerialization() {
        val gson = new GsonBuilder().create();
        assertTrue(gson.htmlSafe());
        setupDriver(gson);

        val person = new Person("Professional Database Breaker", "<>!#$", "@:)", false);
        assertDoesNotThrow(() -> driver.createRecord(personTable, "prince", person));

        val deserializedPerson = driver.retrieveRecord(personTable, "prince");
        assertTrue(deserializedPerson.isPresent());
        // Since person.Name overrides equals, we can use assertEquals
        assertEquals(person.getName(), deserializedPerson.get().getName());
    }

    @Test
    void testInstantSerialization() {
        setupDriver(new Gson());

        Instant now = Instant.now();
        Instant oneDayFromNow = now.plus(1, ChronoUnit.DAYS);

        val instantContainer = InstantContainer.builder()
            .instant(now)
            .instant(oneDayFromNow)
            .build();
        val deserializedDateContainer = driver.createRecord(timeTable, instantContainer);

        assertEquals(instantContainer, deserializedDateContainer);
    }
}
