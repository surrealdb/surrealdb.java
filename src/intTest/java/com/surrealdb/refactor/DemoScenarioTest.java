package com.surrealdb.refactor;

import static com.surrealdb.refactor.Helpers.asMap;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.surrealdb.BaseIntegrationTest;
import com.surrealdb.TestUtils;
import com.surrealdb.connection.SurrealWebSocketConnection;
import com.surrealdb.driver.SyncSurrealDriver;
import com.surrealdb.driver.model.Person;
import com.surrealdb.driver.model.QueryResult;
import com.surrealdb.refactor.driver.BidirectionalSurrealDB;
import com.surrealdb.refactor.driver.SurrealDBFactory;
import com.surrealdb.refactor.driver.UnauthenticatedSurrealDB;
import com.surrealdb.refactor.driver.UnusedSurrealDB;
import com.surrealdb.refactor.driver.parsing.ResultParser;
import com.surrealdb.refactor.types.Credentials;
import com.surrealdb.refactor.types.Param;
import com.surrealdb.refactor.types.QueryBlockResult;
import com.surrealdb.refactor.types.surrealdb.ObjectValue;
import com.surrealdb.refactor.types.surrealdb.Value;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class DemoScenarioTest extends BaseIntegrationTest {

    private SyncSurrealDriver driver;
    private ResultParser resultParser;

    @BeforeEach
    public void setup() {
        final SurrealWebSocketConnection connection =
                new SurrealWebSocketConnection(testHost, testPort, false);
        connection.connect(5);
        driver = new SyncSurrealDriver(connection);

        driver.signIn(TestUtils.getUsername(), TestUtils.getPassword());
        driver.use(TestUtils.getNamespace(), TestUtils.getDatabase());

        driver.create(
                "person:1", new Person("Founder & CEO", "Tobie", "Morgan Hitchcock", true));
        driver.create(
                "person:2", new Person("Founder & COO", "Jaime", "Morgan Hitchcock", true));
    }

    @AfterEach
    public void teardown() {
        driver.delete("person");
        driver.delete("movie");
        driver.delete("message");
        driver.delete("reminder");
    }

    @Test
    public void testSingleQueryResult() {
        // declarations
        final com.surrealdb.refactor.types.QueryResult[] processedOuterResults;
        final Gson gson = new Gson();
        this.resultParser = new ResultParser();

        // given
        final StringBuilder query =
                new StringBuilder("Create person SET title = 'Founder & CEO', ");
        query.append("name.first = 'Tobie', name.last = 'Morgan Hitchcock', marketing = 'true' \n");
        // surrealDB.query
        final Map<String, String> args = new HashMap<>();
        final List<QueryResult<Person>> response =
                driver.query(query.toString(), args, Person.class);
        final Person singlePerson = response.get(0).getResult().get(0);

        // when
        final String resultString = gson.toJson(singlePerson);
        final JsonElement results = JsonParser.parseString(resultString);
        processedOuterResults = resultParser.parseResultMessage(results);

        driver.delete("person");

        // then
        assertEquals(1, processedOuterResults.length);
    }

    @Test
    @Disabled("Flaky on github CI")
    public void testDemoScenario() {
        // Setup
        final URI address = getHttp().orElseThrow(() -> new IllegalStateException("No HTTP server configured"));
        final UnauthenticatedSurrealDB<BidirectionalSurrealDB> unauthenticated =
                new SurrealDBFactory().connectBidirectional(address);

        // Authenticate
        final UnusedSurrealDB<BidirectionalSurrealDB> unusedSurrealDB =
                unauthenticated.authenticate(new Credentials("root", "root"));

        // Use namespace, database
        final BidirectionalSurrealDB surrealDB = unusedSurrealDB.use("test", "test");

        // Create a multi-statement query
        final StringBuilder query =
                new StringBuilder("CREATE person:lamport CONTENT {name: 'leslie'};\n");
        query.append("UPDATE $whichPerson SET year=$year;\n");
        query.append("DELETE person:lamport;");

        // Create the list of parameters used in the query
        final List<Param> params =
                List.of(
                        new Param(
                                "whichPerson", Value.fromJson(new JsonPrimitive("person:lamport"))),
                        new Param("year", Value.fromJson(new JsonPrimitive(2013))));

        // Execute the query
        final QueryBlockResult results = surrealDB.query(query.toString(), params);

        // Validate the results of the first statement in the query
        assertEquals(results.result().size(), 3, results.toString());
        final Value expectedFirstValue =
                new Value(
                        new ObjectValue(
                                asMap(
                                        Tuple.of("name", new Value("leslie")),
                                        Tuple.of("id", new Value("person:lamport")))));
        final List<Value> actual = results.result().get(0).result();
        assertArrayEquals(new Value[] {expectedFirstValue}, actual.toArray(new Value[0]));

        // Validate the results of the second statement in the query
        final Value expectedSecondValue =
                new Value(
                        new ObjectValue(
                                asMap(
                                        Tuple.of("name", new Value("leslie")),
                                        Tuple.of("id", new Value("person:lamport")),
                                        Tuple.of("year", new Value("2013.0")))));
        final List<Value> actualSecondValue = results.result().get(1).result();
        assertArrayEquals(
                new Value[] {expectedSecondValue}, actualSecondValue.toArray(new Value[0]));

        // Validate the results of the third statement in the query
        final List<Value> actualThirdValue = results.result().get(2).result();
        assertArrayEquals(new Value[] {}, actualThirdValue.toArray(new Value[0]));
    }
}
