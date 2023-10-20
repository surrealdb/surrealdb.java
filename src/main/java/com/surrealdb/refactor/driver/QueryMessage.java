package com.surrealdb.refactor.driver;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.surrealdb.refactor.types.Param;
import java.util.List;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class QueryMessage {
    private final String method = "query";
    private final String id;

    private final JsonArray params;

    public QueryMessage(final String requestID, final String query, final List<Param> params) {
        id = requestID;
        final JsonArray requestParamList = new JsonArray();
        // First item in list is string of the query
        requestParamList.add(query);
        // Second item in the list is an object of bindings
        final JsonObject bindings = new JsonObject();
        for (final Param param : params) {
            System.out.printf(
                    "Handling Param %s with id = '%s' and value = '%s'\n",
                    param, param.identifier(), param.value().intoJson());
            bindings.add(param.identifier(), param.value().intoJson());
        }
        requestParamList.add(bindings);
        this.params = requestParamList;
    }
}
