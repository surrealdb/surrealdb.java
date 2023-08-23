package com.surrealdb.refactor;

import com.surrealdb.refactor.types.surrealdb.Value;

import java.util.HashMap;
import java.util.Map;

public class Helpers {
    public static Map<String, Value> asMap(Tuple<String, Value>... data) {
        Map<String, Value> obj = new HashMap<>();
        for (Tuple<String, Value> entry : data) {
            obj.put(entry.key, entry.value);
        }
        return obj;
    }
}
