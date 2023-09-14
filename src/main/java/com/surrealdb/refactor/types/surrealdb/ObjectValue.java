package com.surrealdb.refactor.types.surrealdb;

import java.util.Map;

public record ObjectValue(Map<String, Value> values) {}
