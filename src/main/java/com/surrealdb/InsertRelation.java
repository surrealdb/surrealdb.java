package com.surrealdb;

import java.lang.Object;
import java.util.Objects;

public class InsertRelation {

    public Id id;
    public Thing in;
    public Thing out;

    public InsertRelation() {
    }

    public InsertRelation(Id id, Thing in, Thing out) {
        this.id = id;
        this.in = in;
        this.out = out;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final InsertRelation r = (InsertRelation) o;
        return
            Objects.equals(id, r.id) &&
                Objects.equals(in, r.in) &&
                Objects.deepEquals(out, r.out);
    }

    @Override
    public String toString() {
        return "id: " + id + ", in: " + in + ", out: " + out;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, in, out);
    }
}
