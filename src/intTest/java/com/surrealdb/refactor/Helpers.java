package com.surrealdb.refactor;

import com.surrealdb.refactor.types.surrealdb.Value;
import java.util.HashMap;
import java.util.Map;

public class Helpers {
    @SafeVarargs
    public static Map<String, Value> asMap(final Tuple<String, Value>... data) {
        final Map<String, Value> obj = new HashMap<>();
        for (final Tuple<String, Value> entry : data) {
            obj.put(entry.key, entry.value);
        }
        return obj;
    }
}
