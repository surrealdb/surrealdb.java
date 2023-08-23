package com.surrealdb.refactor;

import static com.surrealdb.refactor.Helpers.asMap;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.surrealdb.BaseIntegrationTest;
import com.surrealdb.refactor.driver.*;
import com.surrealdb.refactor.types.Credentials;
import com.surrealdb.refactor.types.Param;
import com.surrealdb.refactor.types.QueryBlockResult;
import com.surrealdb.refactor.types.surrealdb.Number;
import com.surrealdb.refactor.types.surrealdb.ObjectValue;
import com.surrealdb.refactor.types.surrealdb.Value;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class DemoScenarioTest extends BaseIntegrationTest {
    @Test
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
        Value expectedFirstValue = new Value(new ObjectValue(asMap(
                Tuple.of("name", new Value("leslie")),
                Tuple.of("id", new Value("person:lamport"))
        )));
        List<Value> actual = results.getResult().get(0).getResult();
        assertArrayEquals(new Value[] {expectedFirstValue}, actual.toArray(new Value[0]));

        // Validate the results of the second statement in the query
        Value expectedSecondValue = new Value(new ObjectValue(
                asMap(
                        Tuple.of("name", new Value("leslie")),
                        Tuple.of("id", new Value("person:lamport")),
                        Tuple.of("year", new Value("2013.0"))
                )
        ));
        List<Value> actualSecondValue = results.getResult().get(1).getResult();
        assertArrayEquals(new Value[] {expectedSecondValue}, actualSecondValue.toArray(new Value[0]));

        // Validate the results of the third statement in the query
        List<Value> actualThirdValue = results.getResult().get(2).getResult();
        assertArrayEquals(new Value[] {}, actualThirdValue.toArray(new Value[0]));
    }

}
