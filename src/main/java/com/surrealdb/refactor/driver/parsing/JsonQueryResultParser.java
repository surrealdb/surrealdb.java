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

    private static String type(final JsonElement jsonElement) {
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

    public QueryResult parse(final JsonElement jsonElement) {
        // Test
        if (!jsonElement.isJsonObject()) {
            throw new UnhandledProtocolResponse(
                    String.format("Expected json object, but was %s", type(jsonElement)));
        }
        final JsonObject jsonObject = jsonElement.getAsJsonObject();
        final String status = this.forceGet(
                                jsonObject,
                                "status",
                                new UnhandledProtocolResponse(
                                        "Expected the object to have a status"))
                                  .getAsString();
        final String time = this.forceGet(
                                jsonObject,
                                "time",
                                new UnhandledProtocolResponse("Expected the object to have a time"))
                                .getAsString();
        final JsonElement result = this.forceGet(
                        jsonObject,
                        "result",
                        new UnhandledProtocolResponse("Expected the object to contain a result"));
        final List<Value> valueList = new ArrayList<>();
        if (result.isJsonObject()) {
            final JsonObject object = result.getAsJsonObject();
            valueList.add(this.parseValue(object));
        } else if (result.isJsonArray()) {
            final JsonArray array = result.getAsJsonArray();
            for (final JsonElement arrayElement : array) {
                valueList.add(this.parseValue(arrayElement));
            }
        } else {
            throw new UnhandledProtocolResponse(
                    String.format(
                            "Expected the result type in a result to be an object of array instead was: %s",
                            type(jsonElement)));
        }
        return new QueryResult(valueList, status, time);
    }

    private Value parseValue(final JsonElement jsonElement) {
        if (!jsonElement.isJsonObject()) {
            throw new UnhandledProtocolResponse(
                    String.format(
                            "Expected the result type to be object but was %s", type(jsonElement)));
        }
        final Map<String, Value> objectProperties = new HashMap<>();
        final JsonObject         jsonObject       = jsonElement.getAsJsonObject();
        for (final Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            final String value = entry.getValue().getAsString();
            objectProperties.put(entry.getKey(), new Value(value));
        }
        return new Value(new ObjectValue(objectProperties));
    }

    private JsonElement forceGet(
            final JsonObject jsonObject, final String property, final UnhandledProtocolResponse cause) {
        if (!jsonObject.has(property)) {
            throw cause;
        }
        return jsonObject.get(property);
    }
}
