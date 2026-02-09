package com.surrealdb;

import java.util.Objects;

/**
 * Represents a range of record IDs on a table for range queries (e.g. select/update/delete/upsert
 * over IDs in [start..end] or unbounded on either side).
 * <p>
 * Use {@link Id#from(long)}, {@link Id#from(String)}, or {@link Id#from(java.util.UUID)} for
 * bounds. Pass {@code null} for start or end to make that bound unbounded.
 */
public final class RecordIdRange {

    private final String table;
    private final Id start;
    private final Id end;

    /**
     * Creates a range over the given table with optional start and end bounds.
     *
     * @param table table name
     * @param start lower bound (inclusive), or {@code null} for unbounded
     * @param end   upper bound (inclusive), or {@code null} for unbounded
     */
    public RecordIdRange(String table, Id start, Id end) {
        this.table = Objects.requireNonNull(table, "table");
        this.start = start;
        this.end = end;
    }

    public String getTable() {
        return table;
    }

    /**
     * Lower bound (inclusive), or {@code null} if unbounded.
     */
    public Id getStart() {
        return start;
    }

    /**
     * Upper bound (inclusive), or {@code null} if unbounded.
     */
    public Id getEnd() {
        return end;
    }
}
