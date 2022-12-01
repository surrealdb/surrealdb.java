package meta.tests;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.surrealdb.client.SurrealClient;
import com.surrealdb.client.SurrealClientSettings;
import com.surrealdb.client.SurrealTable;
import lombok.val;
import meta.model.InstantContainer;
import meta.model.Person;
import meta.utils.TestUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * E2E serialization tests
 */
public abstract class SurrealClientGsonTests {

    private static final SurrealTable<Person> personTable = SurrealTable.of("person", Person.class);
    private static final SurrealTable<InstantContainer> timeTable = SurrealTable.of("time", InstantContainer.class);

    private SurrealClient client;

    protected abstract @NotNull SurrealClient createClient(@NotNull SurrealClientSettings settings);

    @AfterEach
    void cleanup() {
        if (client != null) {
            client.deleteAllRecordsInTable(personTable);
            client.deleteAllRecordsInTable(timeTable);

            client.cleanup();
        }
    }

    void createClient(Gson gson) {
        SurrealClientSettings settings = TestUtils.createClientSettingsBuilderWithDefaults()
            .setGson(gson)
            .build();

        client = createClient(settings);

        client.signIn(TestUtils.getAuthCredentials());
        client.setNamespaceAndDatabase(TestUtils.getNamespace(), TestUtils.getDatabase());
    }

    @Test
    void testGsonWithPrettyPrintingDoesNotBreakSerialization() {
        val gson = new GsonBuilder().setPrettyPrinting().create();
        createClient(gson);

        val person = new Person("Contributor", "Damian", "Kocher", false);
        assertDoesNotThrow(() -> client.createRecord(personTable, "damian", person));

        val optionalPersonFromDb = client.retrieveRecord(personTable, "damian");
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
        createClient(gson);

        val person = new Person("Professional Database Breaker", "<>!#$", "@:)", false);
        assertDoesNotThrow(() -> client.createRecord(personTable, "prince", person));

        val deserializedPerson = client.retrieveRecord(personTable, "prince");
        assertTrue(deserializedPerson.isPresent());
        // Since person.Name overrides equals, we can use assertEquals
        assertEquals(person.getName(), deserializedPerson.get().getName());
    }

    @Test
    void testInstantSerialization() {
        createClient(new Gson());

        Instant now = Instant.now();
        Instant oneDayFromNow = now.plus(1, ChronoUnit.DAYS);

        val instantContainer = InstantContainer.builder()
            .instant(now)
            .instant(oneDayFromNow)
            .build();
        val deserializedDateContainer = client.createRecord(timeTable, instantContainer);

        assertEquals(instantContainer, deserializedDateContainer);
    }
}