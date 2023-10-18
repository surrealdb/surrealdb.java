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

    public QueryResult[] parseResultMessage(final JsonElement resultMessage) {
        //  QueryResult[] processedOuterResults;
        final JsonArray outerResultArray;

        if (resultMessage.isJsonObject()) {
            outerResultArray = this.parseResultMessageObject(resultMessage.getAsJsonObject());
        } else if (resultMessage.isJsonArray()) {
            outerResultArray = resultMessage.getAsJsonArray();
        } else {
            throw new RuntimeException("The input json message was neither an object nor an array");
        }

        return this.parseProcessedOuterResults(outerResultArray);
    }

    public JsonArray parseResultMessageObject(final JsonObject resultMessage) {
        final JsonArray innerResultArray = new JsonArray();
        final JsonObject jsonPrimitives = new JsonObject();
        final JsonObject resultObject =
                this.getJsonPrimitives(resultMessage.getAsJsonObject(), jsonPrimitives);

        innerResultArray.add(resultObject);

        // add the status time and result properties to the result message
        final JsonObject preparedJsonObject = new JsonObject();

        preparedJsonObject.addProperty("status", "ok");
        preparedJsonObject.addProperty("time", System.currentTimeMillis());
        preparedJsonObject.add("result", innerResultArray);

        // add prepared object to an array
        final JsonArray outerResultArray = new JsonArray();
        outerResultArray.add(preparedJsonObject);

        return outerResultArray;
    }

    public QueryResult[] parseProcessedOuterResults(final JsonArray outerResultArray) {
        final QueryResult[] processedOuterResults = new QueryResult[outerResultArray.size()];

        for (int i = 0; i < outerResultArray.size(); i++) {
            final JsonElement innerResultJson = outerResultArray.get(i);
            if (!innerResultJson.isJsonObject()) {
                throw new UnhandledProtocolResponse("Expected the result to be an object");
            }
            final QueryResult val = new JsonQueryResultParser().parse(innerResultJson);
            processedOuterResults[i] = val;
        }

        return processedOuterResults;
    }

    public JsonObject getJsonPrimitives(
            final JsonObject oldJsonObject, final JsonObject jsonPrimitives) {

        final Set<Entry<String, JsonElement>> iterableResult = oldJsonObject.entrySet();
        for (final Entry<String, JsonElement> property : iterableResult) {

            if (property.getValue().isJsonPrimitive()) {
                jsonPrimitives.add(property.getKey(), property.getValue());
            } else {
                this.getJsonPrimitives(property.getValue().getAsJsonObject(), jsonPrimitives);
            }
        }

        return jsonPrimitives;
    }
}
