package test.driver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.driver.SyncSurrealDriver;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import test.TestUtils;
import test.driver.model.DateContainer;
import test.driver.model.Person;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

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
            driver.delete("datecontainer");
        }
    }

    void setupDriver(Gson gson) {
        val connectionSettings = TestUtils.createConnectionSettingsBuilderWithDefaults()
            .setGson(gson)
            .setAutoConnect(true)
            .build();

        val connection = SurrealConnection.create(connectionSettings);

        driver = new SyncSurrealDriver(connection);
        driver.signIn(TestUtils.getUsername(), TestUtils.getPassword());
        driver.use(TestUtils.getNamespace(), TestUtils.getDatabase());

    }

    @Test
    void testCustomGsonWithPrettyPrintingEnabledDoesNotThrow() {
        val gson = new GsonBuilder().setPrettyPrinting().create();
        setupDriver(gson);

        val person = new Person("Contributor", "Damian", "Kocher", false);
        assertDoesNotThrow(() -> {
                driver.create("person:damian", person);

            }
        );
    }

    @Test
    void testGsonWithHtmlEscapingDoesNotBreakSerialization() {
        val gson = new GsonBuilder().create();
        assertTrue(gson.htmlSafe());
        setupDriver(gson);

        val person = new Person("Professional Database Breaker", "<>!#$", "@:)", false);
        assertDoesNotThrow(() -> {
                driver.create("person:prince", person);
                val deserializedPerson = driver.select("person:prince", Person.class).get(0);

                // Since person.Name overrides equals, we can use assertEquals
                assertEquals(person.getName(), deserializedPerson.getName());
            }
        );
    }

    @Test
    @Disabled("Disabled until I understand exactly how SurrealDB handles dates")
    void testDateSerialization() {
        setupDriver(new Gson());

        val dateContainer = new DateContainer(Instant.now(), OffsetDateTime.now(), ZonedDateTime.now());
        driver.create("datecontainer:now", dateContainer);
        val deserializedDateContainer = driver.select("datecontainer:now", DateContainer.class).get(0);

        assertEquals(dateContainer, deserializedDateContainer);
    }
}
