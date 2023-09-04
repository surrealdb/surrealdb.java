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
import com.surrealdb.refactor.driver.*;
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
        SurrealWebSocketConnection connection =
                new SurrealWebSocketConnection(testHost, testPort, false);
        connection.connect(5);
        driver = new SyncSurrealDriver(connection);

        driver.signIn(TestUtils.getUsername(), TestUtils.getPassword());
        driver.use(TestUtils.getNamespace(), TestUtils.getDatabase());

        driver.create("person:1", new Person("Founder & CEO", "Tobie", "Morgan Hitchcock", true));
        driver.create("person:2", new Person("Founder & COO", "Jaime", "Morgan Hitchcock", true));
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
        com.surrealdb.refactor.types.QueryResult[] processedOuterResults;
        Gson gson = new Gson();
        this.resultParser = new ResultParser();

        // given
        StringBuilder query = new StringBuilder("Create person SET title = 'Founder & CEO', ");
        query.append("name.first = 'Tobie', name.last = 'Morgan Hitchcock', marketing = 'true' \n");
        // surrealDB.query
        Map<String, String> args = new HashMap<>();
        List<QueryResult<Person>> response = driver.query(query.toString(), args, Person.class);
        Person singlePerson = response.get(0).getResult().get(0);

        // when
        String resultString = gson.toJson(singlePerson);
        JsonElement results = JsonParser.parseString(resultString);
        processedOuterResults = resultParser.parseResultMessage(results);

        driver.delete("person");

        // then
        assertEquals(1, processedOuterResults.length);
        assertEquals("ok", processedOuterResults[0].getStatus());
    }

    @Test
    @Disabled("Flaky on github CI")
    public void testDemoScenario() throws Exception {
        // Setup
        URI address =
                getHttp().orElseThrow(() -> new IllegalStateException("No HTTP server configured"));
        UnauthenticatedSurrealDB<BidirectionalSurrealDB> unauthenticated =
                new SurrealDBFactory().connectBidirectional(address);

        // Authenticate
        UnusedSurrealDB<BidirectionalSurrealDB> unusedSurrealDB =
                unauthenticated.authenticate(new Credentials("root", "root"));

        // Use namespace, database
        BidirectionalSurrealDB surrealDB = unusedSurrealDB.use("test", "test");

        // Create a multi-statement query
        StringBuilder query =
                new StringBuilder("CREATE person:lamport CONTENT {name: 'leslie'};\n");
        query.append("UPDATE $whichPerson SET year=$year;\n");
        query.append("DELETE person:lamport;");

        // Create the list of parameters used in the query
        List<Param> params =
                List.of(
                        new Param(
                                "whichPerson", Value.fromJson(new JsonPrimitive("person:lamport"))),
                        new Param("year", Value.fromJson(new JsonPrimitive(2013))));

        // Execute the query
        QueryBlockResult results = surrealDB.query(query.toString(), params);

        // Validate the results of the first statement in the query
        assertEquals(results.getResult().size(), 3, results.toString());
        Value expectedFirstValue =
                new Value(
                        new ObjectValue(
                                asMap(
                                        Tuple.of("name", new Value("leslie")),
                                        Tuple.of("id", new Value("person:lamport")))));
        List<Value> actual = results.getResult().get(0).getResult();
        assertArrayEquals(new Value[] {expectedFirstValue}, actual.toArray(new Value[0]));

        // Validate the results of the second statement in the query
        Value expectedSecondValue =
                new Value(
                        new ObjectValue(
                                asMap(
                                        Tuple.of("name", new Value("leslie")),
                                        Tuple.of("id", new Value("person:lamport")),
                                        Tuple.of("year", new Value("2013.0")))));
        List<Value> actualSecondValue = results.getResult().get(1).getResult();
        assertArrayEquals(
                new Value[] {expectedSecondValue}, actualSecondValue.toArray(new Value[0]));

        // Validate the results of the third statement in the query
        List<Value> actualThirdValue = results.getResult().get(2).getResult();
        assertArrayEquals(new Value[] {}, actualThirdValue.toArray(new Value[0]));
    }
}
