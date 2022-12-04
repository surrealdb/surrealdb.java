package meta.tests;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.surrealdb.client.SurrealClient;
import com.surrealdb.client.SurrealClientSettings;
import com.surrealdb.types.SurrealTable;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import meta.model.EmptyRecord;
import meta.model.KvMap;
import meta.utils.TestUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static meta.model.KvMap.assertKvMapEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * E2E serialization tests
 */
@SuppressWarnings("OptionalGetWithoutIsPresent")
@Slf4j
public abstract class SurrealClientGsonTests {

    private static final @NotNull String TABLE_NAME = "gson_serialization_tests";

    private @UnknownNullability SurrealClient client;

    protected abstract @NotNull SurrealClient createClient(@NotNull SurrealClientSettings settings);

    void createClient(@NotNull Gson gson) {
        SurrealClientSettings settings = TestUtils.createClientSettingsBuilderWithDefaults()
            .setGson(gson)
            .build();

        client = createClient(settings);

        client.signIn(TestUtils.getAuthCredentials());
        client.setNamespaceAndDatabase(TestUtils.getNamespace(), TestUtils.getDatabase());

        log.info(" --- Finished setup --- ");
    }

    @AfterEach
    void cleanup() {
        if (client != null) {
            log.info(" --- Starting cleanup --- ");

            SurrealTable<EmptyRecord> table = SurrealTable.of(TABLE_NAME, EmptyRecord.class);
            client.deleteAllRecordsInTable(table);

            client.cleanup();
        }
    }

    @Test
    void testGsonWithPrettyPrintingDoesNotBreakSerialization() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        createClient(gson);

        SurrealTable<String2StringMap> table = SurrealTable.of(TABLE_NAME, String2StringMap.class);
        String recordId = "gson_with_pretty_printing";

        String2StringMap object = new String2StringMap();
        object.put("testName", "Gson with pretty printing");
        object.put("testDescription", "This is a test object to verify that Gson's pretty printing does not break serialization");

        String2StringMap setObject = client.setRecord(table, recordId, object);
        String2StringMap retrievedObject = client.retrieveRecord(table, recordId).get();

        assertKvMapEquals(object, setObject);
        assertKvMapEquals(object, retrievedObject);
    }

    @Test
    void testGsonWithHtmlEscapingDoesNotBreakSerialization() {
        Gson gson = new Gson();
        assertTrue(gson.htmlSafe());
        createClient(gson);

        SurrealTable<String2StringMap> table = SurrealTable.of(TABLE_NAME, String2StringMap.class);
        String recordId = "dangerous_strings";

        String2StringMap object = new String2StringMap();
        object.put("backslash", "\\");
        object.put("double_quote", "\"");
        object.put("single_quote", "'");
        object.put("ampersand", "&");
        object.put("less_than", "<");
        object.put("greater_than", ">");
        object.put("some_html", "<html><body><h1>Some HTML</h1></body></html>");

        String2StringMap setObject = client.setRecord(table, recordId, object);
        String2StringMap retrievedObject = client.retrieveRecord(table, recordId).get();

        assertKvMapEquals(object, setObject);
        assertKvMapEquals(object, retrievedObject);
    }

    @Test
    void testInstantSerialization() {
        val gson = new Gson();
        createClient(gson);

        SurrealTable<String2InstantMap> table = SurrealTable.of("gson_serialization_test", String2InstantMap.class);

        String recordId = "instant_serialization";

        String2InstantMap instants = new String2InstantMap();
        instants.put("now", Instant.now());
        instants.put("epoch", Instant.EPOCH);
        instants.put("min", Instant.MIN);
        instants.put("max", Instant.MAX);
        instants.put("precision", Instant.ofEpochSecond(1670093501, 123456789));

        String2InstantMap setObject = client.setRecord(table, recordId, instants);
        String2InstantMap retrievedObject = client.retrieveRecord(table, recordId).get();

        assertKvMapEquals(instants, setObject);
        assertKvMapEquals(instants, retrievedObject);
    }

    private static class String2StringMap extends KvMap<String, String> {
    }

    private static class String2InstantMap extends KvMap<String, Instant> {
    }
}
