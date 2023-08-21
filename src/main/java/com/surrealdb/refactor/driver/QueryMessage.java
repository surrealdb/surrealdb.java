package com.surrealdb.refactor.driver;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.surrealdb.refactor.types.Param;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@ToString
public class QueryMessage {
    private final String method = "query";
    private final String id;

    private final String[] params;

    public QueryMessage(String requestID, String query, List<Param> params) {
        id = requestID;
        List<String> list = new ArrayList<>();
        list.add(query);
        for (Param param: params) {
            JsonObject paramEntry = new JsonObject();
            paramEntry.add(param.getIdentifier(), param.getValue().intoJson());
            list.add(paramEntry.toString());
        }
        this.params = list.toArray(new String[0]);
    }
}
