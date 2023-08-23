package com.surrealdb.refactor.types;

import java.util.List;
import lombok.ToString;

@ToString
public class QueryBlockResult {
    private final List<QueryResult> result;

    public QueryBlockResult(List<QueryResult> result) {
        this.result = result;
    }

    /**
     * Get all the query results from the query
     *
     * @return the result of each individual statement in a query block
     */
    public List<QueryResult> getResult() {
        return result;
    }
}
