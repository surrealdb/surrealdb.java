package com.surrealdb.pojos;

import java.util.Objects;

public class Review {

    public Integer rate;
    public String comment;

    public Review(Integer rate, String comment) {
        this.rate = rate;
        this.comment = comment;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Review r = (Review) o;
        return
            Objects.equals(rate, r.rate) &&
                Objects.equals(comment, r.comment);
    }

    @Override
    public String toString() {
        return "rate: " + rate + ", comment: " + comment;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rate, comment);
    }
}
