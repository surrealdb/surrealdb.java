package com.surrealdb.refactor.types.surrealdb;

import lombok.Getter;
import lombok.ToString;

import java.util.Optional;

@ToString
public class Number {
    private final Float _float;

    public Number(float v) {
        _float = v;
    }

    public boolean isFloat() {
        return _float!=null;
    }

    public Optional<Float> asFloat() {
        return Optional.ofNullable(_float);
    }

}
