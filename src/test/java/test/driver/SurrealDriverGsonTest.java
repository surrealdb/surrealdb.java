package test.driver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.surrealdb.connection.SurrealConnection;
import com.surrealdb.connection.SurrealConnectionSettings;
import com.surrealdb.driver.SyncSurrealDriver;
import org.junit.jupiter.api.Test;
import test.TestUtils;
import test.driver.model.Person;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class SurrealDriverGsonTest {

    private static SurrealConnectionSettings getConnectionSettings(Gson gson) {
        return TestUtils.createConnectionSettingsBuilderWithDefaults()
            .setAutoConnect(true)
            .setGson(gson)
            .build();
    }

    @Test
    void testCustomGsonWithPrettyPrintingEnabledDoesNotThrow() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        SurrealConnection connection = SurrealConnection.create(getConnectionSettings(gson));
        SyncSurrealDriver driver = new SyncSurrealDriver(connection);

        driver.signIn(TestUtils.getUsername(), TestUtils.getPassword());
        driver.use(TestUtils.getNamespace(), TestUtils.getDatabase());

        Person person = new Person("Contributor", "Damian", "Kocher", false);
        assertDoesNotThrow(() -> {
                driver.create("person:damian", person);
                driver.delete("person:damian");
            }
        );
    }
}
