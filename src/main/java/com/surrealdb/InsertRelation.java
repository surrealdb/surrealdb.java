package com.surrealdb;

import java.lang.Object;
import java.util.Objects;

/**
 * The InsertRelation class represents a relationship between entities in a graph database.
 * It is primarily used to insert relations into a table using SurrealDB.
 * <p>
 * This class encapsulates the identifiers for the relation, including the IDs for the entities that
 * participate in the relationship. Specifically, it includes the following fields:
 * <ul>
 * <li>id: The unique identifier for the relation</li>
 * <li>in: The RecordId of the incoming node in the relation</li>
 * <li>out: The RecordId of the outgoing node in the relation</li>
 * </ul>
 * <p>
 */
public class InsertRelation {

    public Id id;
    public RecordId in;
    public RecordId out;

    public InsertRelation() {
    }

    public InsertRelation(Id id, RecordId in, RecordId out) {
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
