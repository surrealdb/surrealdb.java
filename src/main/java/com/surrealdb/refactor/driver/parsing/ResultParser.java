package com.surrealdb.refactor.driver.parsing;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.surrealdb.refactor.exception.UnhandledProtocolResponse;
import com.surrealdb.refactor.types.QueryResult;

public class ResultParser {
    // this method parses the JSON element regardless of whether it is a single element
    // or an array of elements

    public ResultParser() {}

    public QueryResult[] parseResultMessage(JsonElement resultMessage) {
        QueryResult[] processedOuterResults;

        // checks to see if result is a JSON element, if not throw exception
        if (resultMessage.isJsonObject()) {
            JsonArray outerResultArray;

            if (!resultMessage.isJsonArray()) {

                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("status", "ok");
                jsonObject.addProperty("time", System.currentTimeMillis());
                jsonObject.add("result", resultMessage.getAsJsonObject());

                // add element to an array if it is not an array
                outerResultArray = new JsonArray();
                outerResultArray.add(jsonObject);

            } else if (resultMessage.isJsonArray()) {

                outerResultArray = resultMessage.getAsJsonArray();
            } else {
                // this puts in the else if for the if it is a jsonArray; however,
                // I am unsure of when this portion of the code would run
                throw new RuntimeException(
                        "https://github.com/surrealdb/surrealdb.java/issues/75\n "
                                + "The Outer Result Array came up for false for (1) is not an"
                                + " Array and (2) is an Array");
            }

            processedOuterResults = new QueryResult[outerResultArray.size()];
            for (int i = 0; i < outerResultArray.size(); i++) {
                JsonElement innerResultJson = outerResultArray.get(i);
                if (!innerResultJson.isJsonObject()) {
                    throw new UnhandledProtocolResponse("Expected the result to be an object");
                }

                QueryResult val = new JsonQueryResultParser().parse(innerResultJson);
                processedOuterResults[i] = val;
            }
        } else {

            throw new UnhandledProtocolResponse(
                    "https://github.com/surrealdb/surrealdb.java/issues/75\n"
                            + "The response contained results that were not a Json Element");
        }
        return processedOuterResults;
    }
}
