package com.surrealdb.pojos;

import java.util.Objects;

public class Name {
    public String first;
    public String last;

    public Name() {
    }

    public Name(String first, String last) {
        this.first = first;
        this.last = last;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Name name = (Name) o;
        return name.first.equals(first) && name.last.equals(last);
    }

    @Override
    public String toString() {
        return "first=" + first + ", last=" + last;
    }

    @Override
    public int hashCode() {
        return Objects.hash(first, last);
    }
}
