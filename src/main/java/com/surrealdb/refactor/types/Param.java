package com.surrealdb.refactor.types;

import com.surrealdb.refactor.types.surrealdb.Value;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Param {
    private final String identifier;
    private final Value value;

    public Param(String identifier, Value value) {
        this.identifier = identifier;
        this.value = value;
    }
}
