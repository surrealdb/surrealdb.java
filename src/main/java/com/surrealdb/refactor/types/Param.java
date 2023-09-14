package com.surrealdb.refactor.types;

import com.surrealdb.refactor.types.surrealdb.Value;

public record Param(String identifier, Value value) {}
