package com.surrealdb.refactor.driver.parsing;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.surrealdb.refactor.exception.UnhandledProtocolResponse;
import com.surrealdb.refactor.types.QueryResult;
import java.util.Map.Entry;
import java.util.Set;

public class ResultParser {
    // this method parses the JSON element regardless of whether it is a single element
    // or an array of elements

    public ResultParser() {}

    public QueryResult[] parseResultMessage(JsonElement resultMessage) {
        //  QueryResult[] processedOuterResults;
        JsonArray outerResultArray;

        if (resultMessage.isJsonObject()) {
            outerResultArray = this.parseResultMessageObject(resultMessage.getAsJsonObject());
        } else if (resultMessage.isJsonArray()) {
            outerResultArray = resultMessage.getAsJsonArray();
        } else {
            throw new RuntimeException("The input json message was neither an object nor an array");
        }

        return this.parseProcessedOuterResults(outerResultArray);
    }

    public JsonArray parseResultMessageObject(JsonObject resultMessage) {
        JsonArray innerResultArray = new JsonArray();
        JsonObject jsonPrimitives = new JsonObject();
        JsonObject resultObject =
                getJsonPrimitives(resultMessage.getAsJsonObject(), jsonPrimitives);

        innerResultArray.add(resultObject);

        // add the status time and result properties to the result message
        JsonObject preparedJsonObject = new JsonObject();

        preparedJsonObject.addProperty("status", "ok");
        preparedJsonObject.addProperty("time", System.currentTimeMillis());
        preparedJsonObject.add("result", innerResultArray);

        // add prepared object to an array
        JsonArray outerResultArray = new JsonArray();
        outerResultArray.add(preparedJsonObject);

        return outerResultArray;
    }

    public QueryResult[] parseProcessedOuterResults(JsonArray outerResultArray) {
        QueryResult[] processedOuterResults = new QueryResult[outerResultArray.size()];

        for (int i = 0; i < outerResultArray.size(); i++) {
            JsonElement innerResultJson = outerResultArray.get(i);
            if (!innerResultJson.isJsonObject()) {
                throw new UnhandledProtocolResponse("Expected the result to be an object");
            }
            QueryResult val = new JsonQueryResultParser().parse(innerResultJson);
            processedOuterResults[i] = val;
        }

        return processedOuterResults;
    }

    public JsonObject getJsonPrimitives(JsonObject oldJsonObject, JsonObject jsonPrimitives) {

        Set<Entry<String, JsonElement>> iterableResult = oldJsonObject.entrySet();
        for (Entry<String, JsonElement> property : iterableResult) {

            if (property.getValue().isJsonPrimitive()) {
                jsonPrimitives.add(property.getKey(), property.getValue());
            } else {
                getJsonPrimitives(property.getValue().getAsJsonObject(), jsonPrimitives);
            }
        }

        return jsonPrimitives;
    }
}
