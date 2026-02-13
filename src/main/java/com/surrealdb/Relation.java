package com.surrealdb;

import java.util.Objects;

/**
 * The Relation class represents a relationship between two records within a
 * database. It holds the identifiers for the relationship itself and the two
 * records it connects.
 */
public class Relation {

	public RecordId id;
	public RecordId in;
	public RecordId out;

	public Relation() {
	}

	public Relation(RecordId id, RecordId in, RecordId out) {
		this.id = id;
		this.in = in;
		this.out = out;
	}

	@Override
	public boolean equals(java.lang.Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Relation))
			return false;
		Relation r = (Relation) obj;
		return Objects.equals(id, r.id) && Objects.equals(in, r.in) && Objects.deepEquals(out, r.out);
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
