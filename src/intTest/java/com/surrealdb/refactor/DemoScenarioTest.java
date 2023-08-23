package com.surrealdb.refactor;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.surrealdb.BaseIntegrationTest;
import com.surrealdb.refactor.driver.*;
import com.surrealdb.refactor.types.Credentials;
import com.surrealdb.refactor.types.Param;
import com.surrealdb.refactor.types.QueryBlockResult;
import com.surrealdb.refactor.types.surrealdb.Value;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class DemoScenarioTest extends BaseIntegrationTest {
    @Test
//    @Disabled("Functionality is unimplemented, but having the tests shows the design")
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

        // Validate the results of the multi-statement query
        assertEquals(results.getResult().size(), 3, results.toString());
        assertEquals(
                results.getResult().get(0).getResult().get(0).intoJson(),
                asJson(
                        Tuple.of("name", new JsonPrimitive("leslie")),
                        Tuple.of("id", new JsonPrimitive("person:lamport"))));
        assertEquals(
                results.getResult().get(1).getResult().get(0).intoJson(),
                asJson(
                        Tuple.of("name", new JsonPrimitive("leslie")),
                        Tuple.of("id", new JsonPrimitive("person:lamport"))));
    }

    // ----------------------------------------------------------------
    // Helpers below this point

    private static JsonObject asJson(Tuple<String, JsonElement>... data) {
        JsonObject obj = new JsonObject();
        for (Tuple<String, JsonElement> entry : data) {
            obj.add(entry.key, entry.value);
        }
        return obj;
    }
}
