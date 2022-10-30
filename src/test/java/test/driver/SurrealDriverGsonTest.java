package test.driver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.driver.SyncSurrealDriver;
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
 * @author Damian Kocher
 */
public class SurrealDriverGsonTest {

    private SyncSurrealDriver driver;

    @AfterEach
    void cleanup() {
        if (driver != null) {
            driver.delete("person");
            driver.delete("InstantContainer");
        }
    }

    void setupDriver(Gson gson) {
        val connectionSettings = TestUtils.createConnectionSettingsBuilderWithDefaults()
            .setGson(gson)
            .setAutoConnect(true)
            .build();

        val connection = SurrealConnection.create(connectionSettings);

        driver = new SyncSurrealDriver(connection);
        driver.signInAsRootUser(TestUtils.getUsername(), TestUtils.getPassword());
        driver.use(TestUtils.getNamespace(), TestUtils.getDatabase());
    }

    @Test
    void testGsonWithPrettyPrintingDoesNotBreakSerialization() {
        val gson = new GsonBuilder().setPrettyPrinting().create();
        setupDriver(gson);

        val person = new Person("Contributor", "Damian", "Kocher", false);
        assertDoesNotThrow(() -> driver.create("person:damian", person));

        val personFromDb = driver.select("person:damian", Person.class).get(0);

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
        assertDoesNotThrow(() -> driver.create("person:prince", person));

        val deserializedPerson = driver.select("person:prince", Person.class).get(0);
        // Since person.Name overrides equals, we can use assertEquals
        assertEquals(person.getName(), deserializedPerson.getName());
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
        val deserializedDateContainer = driver.create("InstantContainer", instantContainer);

        assertEquals(instantContainer, deserializedDateContainer);
    }
}
