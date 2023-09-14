package com.surrealdb.refactor.types.surrealdb;

import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Number {
    private final Float _float;

    public Number(final float v) {
        this._float = v;
    }

    public boolean isFloat() {
        return this._float != null;
    }

    public Optional<Float> asFloat() {
        return Optional.ofNullable(this._float);
    }
}
