package com.surrealdb.pojos;

import com.surrealdb.Relation;
import com.surrealdb.Thing;

import java.util.Objects;

public class ReviewRelation extends Relation {

    public Integer rate;
    public String comment;

    public ReviewRelation() {
    }

    public ReviewRelation(Thing id, Thing in, Thing out, Integer rate, String comment) {
        super(id, in, out);
        this.rate = rate;
        this.comment = comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ReviewRelation r = (ReviewRelation) o;
        return super.equals(o) &&
            Objects.equals(rate, r.rate) &&
            Objects.equals(comment, r.comment);
    }

    @Override
    public String toString() {
        return super.toString() + ", rate: " + rate + ", comment: " + comment;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, in, out, rate, comment);
    }
}
