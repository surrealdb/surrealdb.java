package com.surrealdb.refactor.types;

import com.surrealdb.refactor.types.surrealdb.Value;
import java.util.List;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class QueryResult {
    private final List<Value> result;
    private final String status;
    private final String time;

    public QueryResult(List<Value> result, String status, String time) {
        this.result = result;
        this.status = status;
        this.time = time;
    }
}
