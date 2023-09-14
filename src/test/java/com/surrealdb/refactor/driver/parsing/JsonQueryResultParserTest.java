package com.surrealdb.refactor.driver.parsing;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.surrealdb.refactor.types.QueryResult;
import com.surrealdb.refactor.types.surrealdb.ObjectValue;
import com.surrealdb.refactor.types.surrealdb.Value;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class JsonQueryResultParserTest {

    @SafeVarargs
    private static Map<String, Value> asMap(final Tuple<String, Value>... data) {
        final Map<String, Value> obj = new HashMap<>();
        for (final Tuple<String, Value> entry : data) {
            obj.put(entry.key, entry.value);
        }
        return obj;
    }

    @Test
    public void regular_result() {
        final String rawJson =
                """
                {"result":[{"id":"person:lamport","name":"leslie"}],"status":"OK","time":"438.583µs"}
                """;

        final JsonElement jsonElement = JsonParser.parseString(rawJson);
        final QueryResult res = new JsonQueryResultParser().parse(jsonElement);

        assertEquals("OK", res.getStatus());
        assertEquals("438.583µs", res.getTime());
        final Value expectedValue =
                new Value(
                        new ObjectValue(
                                asMap(
                                        Tuple.of("id", new Value("person:lamport")),
                                        Tuple.of("name", new Value("leslie")))));
        assertArrayEquals(new Value[] {expectedValue}, res.getResult().toArray(new Value[0]));
    }
}
