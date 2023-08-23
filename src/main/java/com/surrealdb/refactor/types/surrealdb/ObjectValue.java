package com.surrealdb.refactor.types.surrealdb;

import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
public class ObjectValue {
    private final Map<String, Value> values;

    public ObjectValue(Map<String, Value> values) {
        this.values = values;
    }
}
