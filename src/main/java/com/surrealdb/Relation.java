package com.surrealdb;

import java.lang.Object;
import java.util.Objects;

public class Relation {

    public Thing id;
    public Thing in;
    public Thing out;

    public Relation() {
    }

    public Relation(Thing id, Thing in, Thing out) {
        this.id = id;
        this.in = in;
        this.out = out;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Relation r = (Relation) o;
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
