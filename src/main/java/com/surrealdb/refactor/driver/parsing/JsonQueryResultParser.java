package com.surrealdb.refactor.driver.parsing;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.surrealdb.refactor.exception.SurrealDBException;
import com.surrealdb.refactor.exception.UnhandledProtocolResponse;
import com.surrealdb.refactor.types.QueryResult;
import com.surrealdb.refactor.types.surrealdb.ObjectValue;
import com.surrealdb.refactor.types.surrealdb.Value;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonQueryResultParser {
    private static final String ARRAY = "Array";
    private static final String OBJECT = "Object";
    private static final String PRIMTIVE = "Primitive";
    private static final String NULL = "Null";

    public QueryResult parse(JsonElement jsonElement) {
        // Test
        if (!jsonElement.isJsonObject()) {
            throw new UnhandledProtocolResponse(
                    String.format("Expected json object, but was %s", type(jsonElement)));
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String status =
                forceGet(
                                jsonObject,
                                "status",
                                new UnhandledProtocolResponse(
                                        "Expected the object to have a status"))
                        .getAsString();
        String time =
                forceGet(
                                jsonObject,
                                "time",
                                new UnhandledProtocolResponse("Expected the object to have a time"))
                        .getAsString();
        JsonElement result =
                forceGet(
                        jsonObject,
                        "result",
                        new UnhandledProtocolResponse("Expected the object to contain a result"));
        List<Value> valueList = new ArrayList<>();
        if (result.isJsonObject()) {
            JsonObject object = result.getAsJsonObject();
            valueList.add(parseValue(object));
        } else if (result.isJsonArray()) {
            JsonArray array = result.getAsJsonArray();
            for (JsonElement arrayElement : array) {
                valueList.add(parseValue(arrayElement));
            }
        } else {
            throw new UnhandledProtocolResponse(
                    String.format(
                            "Expected the result type in a result to be an object of array instead was: %s",
                            type(jsonElement)));
        }
        return new QueryResult(valueList, status, time);
    }

    private Value parseValue(JsonElement jsonElement) {
        if (!jsonElement.isJsonObject()) {
            throw new UnhandledProtocolResponse(
                    String.format(
                            "Expected the result type to be object but was %s", type(jsonElement)));
        }
        Map<String, Value> objectProperties = new HashMap<>();
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String value = entry.getValue().getAsString();
            objectProperties.put(entry.getKey(), new Value(value));
        }
        return new Value(new ObjectValue(objectProperties));
    }

    private JsonElement forceGet(
            JsonObject jsonObject, String property, UnhandledProtocolResponse cause) {
        if (!jsonObject.has(property)) {
            throw cause;
        }
        return jsonObject.get(property);
    }

    private static String type(JsonElement jsonElement) {
        if (jsonElement.isJsonArray()) {
            return ARRAY;
        } else if (jsonElement.isJsonObject()) {
            return OBJECT;
        } else if (jsonElement.isJsonNull()) {
            return NULL;
        } else if (jsonElement.isJsonPrimitive()) {
            return PRIMTIVE;
        }
        throw new SurrealDBException("There is an unknown JsonElement type");
    }
}
