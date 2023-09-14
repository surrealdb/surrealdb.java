package com.surrealdb.refactor.types;

import com.surrealdb.refactor.types.surrealdb.Value;
import java.util.List;

public record QueryResult(List<Value> result, String status, String time) {}
