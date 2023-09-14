package com.surrealdb.refactor.types;

import com.surrealdb.refactor.types.surrealdb.Value;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@Getter
@ToString
public class QueryResult {
    private final List<Value> result;
    private final String status;
    private final String time;

    public QueryResult(final List<Value> result, final String status, final String time) {
        this.result = result;
        this.status = status;
        this.time = time;
    }
}
