package com.surrealdb.pojos;

import com.surrealdb.Value;

import java.util.Objects;

public class Partial {

    public Value inner;

    @Override
    public int hashCode() {
        return Objects.hash(inner);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Partial d = (Partial) o;
        return
            Objects.equals(inner, d.inner);
    }

    @Override
    public String toString() {
        return "inner: " + inner;
    }

}
